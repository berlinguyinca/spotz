package com.eharmony.spotz.objective.vw

import java.io.{PrintWriter, File}

import com.eharmony.spotz.objective.SparkObjective
import com.eharmony.spotz.space.Point
import com.eharmony.spotz.util.FileUtil
import org.apache.spark.{Logging, SparkFiles, SparkContext}

import scala.collection.{immutable, mutable}
import scala.io.Source

/**
 *
 * @author vsuthichai
 */
class SparkVwCrossValidationObjective(
    @transient sc: SparkContext,
    numFolds: Int,
    vwInputPath: String,
    extraVwParams: String)
  extends SparkObjective[Point, Double](sc) with Logging {

  private[this] val foldToVwCacheFiles = prepareVwInput(vwInputPath)

  /**
   * This method takes the VW input file specified in the class constructor and
   * partitions the file into a training set and test set for every fold.  The train
   * and test set for every fold are then input into VW to generate cache files.
   * These cache files are added to the SparkContext so that they'll
   * be accessible on the executors. To keep track of the train and test set caches
   * for every fold, a Map is used where the key is the fold number and the value is
   * (trainingSetCachePath, testSetCachePath).  These file names do NEED to be unique so that
   * they do not collide with other file names.  The entirety of this method
   * runs on the driver.  All VW input training / test set files as well as cache
   * files are deleted upon JVM exit.
   *
   * This strategy has the downside of duplicating the dataset across every node K times.
   * An alternative approach is to train K cache files and train the regressor K - 1 times and
   * test on the last test cache file.
   *
   * @return a map representation where key is the fold number and value is
   *         (trainingSetFilename, testSetFilename)
   */
  private[this] def prepareVwInput(inputPath: String): immutable.Map[Int, (String, String)] = {
    val enumeratedVwInput = Source.fromFile(inputPath).getLines().zipWithIndex.toList

    // For every fold iteration, partition the vw input such that one fold is the test set and the
    // remaining K-1 folds comprise the training set
    (0 to numFolds - 1).foldLeft(mutable.Map[Int, (String, String)]()) { (map, fold) =>
      val (train, test) = enumeratedVwInput.partition {
        // train
        case (line, lineNumber) if lineNumber % numFolds != fold => true
        // test
        case (line, lineNumber) if lineNumber % numFolds == fold => false
      }

      // Write out training set for this fold
      val vwTrainingSetFile   = FileUtil.tempFile(s"train-$fold-", ".vw")
      val vwTrainingSetPath   = vwTrainingSetFile.getAbsolutePath
      val vwTrainingSetWriter = new PrintWriter(vwTrainingSetFile)

      train.foreach { case (line, lineNumber) => vwTrainingSetWriter.println(line) }
      vwTrainingSetWriter.close()

      // Write out training cache file with a unique filename and add it to spark context
      val vwTrainingCacheFile = FileUtil.tempFile(s"train-cache-$fold-", ".cache")
      val vwTrainingCachePath = vwTrainingCacheFile.getAbsolutePath
      val vwTrainingProcess   = VwProcess(s"-k --cache_file $vwTrainingCachePath -d $vwTrainingSetPath")
      val vwTrainingResult    = vwTrainingProcess()

      assert(vwTrainingResult.exitCode == 0,
             s"VW Training cache exited with non-zero exit code ${vwTrainingResult.exitCode}")
      sc.addFile(vwTrainingCachePath)

      // Write out test set for this fold and add it to the spark context
      val testFoldFile   = FileUtil.tempFile(s"test-$fold-", ".vw")
      val testFoldPath   = testFoldFile.getAbsolutePath
      val testFoldWriter = new PrintWriter(testFoldFile)

      test.foreach { case (line, lineNumber) => testFoldWriter.println(line) }
      testFoldWriter.close()

      // Write out test cache file with a unique filename and add it to spark context
      val vwTestCacheFile = FileUtil.tempFile(s"test-cache-$fold-", ".cache")
      val vwTestCachePath = vwTestCacheFile.getAbsolutePath
      val vwTestProcess   = VwProcess(s"-k --cache_file $vwTestCachePath -d $testFoldPath")
      val vwTestResult    = vwTestProcess()

      assert(vwTestResult.exitCode == 0,
             s"VW Test Cache exited with non-zero exit code ${vwTrainingResult.exitCode}")
      sc.addFile(vwTestCachePath)

      // Add it to the map which will be referenced later on the executor
      map += ((fold, (vwTrainingCacheFile.getName, vwTestCacheFile.getName)))
    }.toMap
  }

  /**
   * This method can run on the driver and/or the executor.  It performs a k-fold cross validation
   * over the vw input dataset passed through the class constructor.  The dataset has been split in
   * such a way that every fold has its own training and test set in the form of VW cache files.
   *
   * @param point a point object representing the hyper parameters to evaluate upon
   * @return Double the cross validated average loss
   */
  override def apply(point: Point): Double = {
    val l = point.get("l")

    val avgLosses = (0 to numFolds - 1).map { fold =>
      // Retrieve the training and test set cache for this fold.
      val (vwTrainingFilename, vwTestFilename) = foldToVwCacheFiles(fold)
      val vwTrainingFile = SparkFiles.get(vwTrainingFilename)
      val vwTestFile = SparkFiles.get(vwTestFilename)

      // Initialize the model file on the filesystem.  Just reserve a unique filename.
      val modelFile = FileUtil.tempFile(s"model-$fold-", ".vw")

      // Train
      val vwTrainingProcess = VwProcess(s"-l $l -f ${modelFile.getAbsolutePath} --cache_file $vwTrainingFile $extraVwParams")
      logInfo(s"Executing training: ${vwTrainingProcess.toString}")
      val vwTrainResult = vwTrainingProcess()
      logInfo(s"Train stderr ${vwTrainResult.stderr}")
      assert(vwTrainResult.exitCode == 0, s"VW Training exited with non-zero exit code s${vwTrainResult.exitCode}")

      // Test
      val vwTestProcess = VwProcess(s"-t -i ${modelFile.getAbsolutePath} --cache_file $vwTestFile $extraVwParams")
      logInfo(s"Executing testing: ${vwTestProcess.toString}")
      val vwTestResult = vwTestProcess()
      assert(vwTestResult.exitCode == 0, s"VW Testing exited with non-zero exit code s${vwTestResult.exitCode}")
      logInfo(s"Test stderr ${vwTestResult.stderr}")
      val loss = vwTestResult.loss.getOrElse(throw new RuntimeException("Unable to obtain avg loss from test result"))

      // Delete the model.  We don't need these sitting around on the executor's filesystem
      // since they can pile up pretty quickly across many cross validation folds.
      modelFile.delete()

      loss
    }

    logInfo(s"Avg losses for all folds: $avgLosses")
    val crossValidatedAvgLoss = avgLosses.sum / numFolds
    logInfo(s"Cross validated avg loss: $crossValidatedAvgLoss")

    crossValidatedAvgLoss
  }
}

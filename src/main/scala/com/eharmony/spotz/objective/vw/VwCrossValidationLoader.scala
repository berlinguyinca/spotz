package com.eharmony.spotz.objective.vw

import java.io.PrintWriter

import com.eharmony.spotz.util.FileUtil
import org.apache.spark.SparkContext

import scala.collection.mutable
import scala.io.Source

/**
  * @author vsuthichai
  */
trait VwCrossValidationLoader {
  @transient val sc: SparkContext

  def prepareVwInput(inputPath: String): Map[Int, (String, String)]
}

trait VwCrossValidationLoaderImpl extends VwCrossValidationLoader {
  val numFolds: Int
  val vwInputPath: String

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
  override def prepareVwInput(inputPath: String): Map[Int, (String, String)] = {
    val enumeratedVwInput = Source.fromInputStream(FileUtil.loadFile(inputPath)).getLines().zipWithIndex.toList

    // For every fold iteration, partition the vw input such that one fold is the test set and the
    // remaining K-1 folds comprise the training set
    (0 until numFolds).foldLeft(mutable.Map[Int, (String, String)]()) { (map, fold) =>

      val (train, test) = enumeratedVwInput.partition {
        // train
        case (line, lineNumber) if lineNumber % numFolds != fold => true
        // test
        case (line, lineNumber) if lineNumber % numFolds == fold => false
      }

      // Write out training set for this fold
      val vwTrainingSetFile = FileUtil.tempFile(s"train-$fold-", ".vw")
      val vwTrainingSetPath = vwTrainingSetFile.getAbsolutePath
      val vwTrainingSetWriter = new PrintWriter(vwTrainingSetFile)

      train.foreach { case (line, lineNumber) => vwTrainingSetWriter.println(line) }
      vwTrainingSetWriter.close()

      // Write out training cache file with a unique filename and add it to spark context
      val vwTrainingCacheFile = FileUtil.tempFile(s"train-cache-$fold-", ".cache")
      val vwTrainingCachePath = vwTrainingCacheFile.getAbsolutePath
      val vwTrainingProcess = VwProcess(s"-k --cache_file $vwTrainingCachePath -d $vwTrainingSetPath")
      val vwTrainingResult = vwTrainingProcess()

      assert(vwTrainingResult.exitCode == 0,
        s"VW Training cache exited with non-zero exit code ${vwTrainingResult.exitCode}")
      sc.addFile(vwTrainingCachePath)

      // Write out test set for this fold and add it to the spark context
      val testFoldFile = FileUtil.tempFile(s"test-$fold-", ".vw")
      val testFoldPath = testFoldFile.getAbsolutePath
      val testFoldWriter = new PrintWriter(testFoldFile)

      test.foreach { case (line, lineNumber) => testFoldWriter.println(line) }
      testFoldWriter.close()

      // Write out test cache file with a unique filename and add it to spark context
      val vwTestCacheFile = FileUtil.tempFile(s"test-cache-$fold-", ".cache")
      val vwTestCachePath = vwTestCacheFile.getAbsolutePath
      val vwTestProcess = VwProcess(s"-k --cache_file $vwTestCachePath -d $testFoldPath")
      val vwTestResult = vwTestProcess()

      assert(vwTestResult.exitCode == 0,
        s"VW Test Cache exited with non-zero exit code ${vwTrainingResult.exitCode}")
      sc.addFile(vwTestCachePath)

      // Add it to the map which will be referenced later on the executor
      map += ((fold, (vwTrainingCacheFile.getName, vwTestCacheFile.getName)))
    }.toMap
  }
}

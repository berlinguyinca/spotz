package com.eharmony.spotz.objective.vw

import com.eharmony.spotz.util.FileUtil
import org.apache.spark.SparkContext

import scala.collection.mutable
import scala.io.Source

/**
  * @author vsuthichai
  */
trait VwDatasetLoader extends VwSparkFunctions {
  @transient val sc: SparkContext

  /**
    *
    * @param inputPath
    * @return
    */
  def prepareVwInput(inputPath: String): String = {
    val dataset = Source.fromInputStream(FileUtil.loadFile(inputPath)).getLines().toIterable
    addDatasetToSpark(dataset, "dataset.cache")
  }
}

trait VwCrossValidationLoader extends VwSparkFunctions {
  @transient val sc: SparkContext
  val numFolds: Int
  val vwDatasetPath: String

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
  def prepareVwInput(inputPath: String): Map[Int, (String, String)] = {
    val enumeratedVwInput = Source.fromInputStream(FileUtil.loadFile(inputPath)).getLines().zipWithIndex.toList

    // For every fold iteration, partition the vw input such that one fold is the test set and the
    // remaining K-1 folds comprise the training set
    (0 until numFolds).foldLeft(mutable.Map[Int, (String, String)]()) { (map, fold) =>
      val (trainWithLineNumber, testWithLineNumber) = enumeratedVwInput.partition {
        // train
        case (line, lineNumber) if lineNumber % numFolds != fold => true
        // test
        case (line, lineNumber) if lineNumber % numFolds == fold => false
      }

      val train = trainWithLineNumber.map { case (line, lineNumber) => line }
      val vwTrainingCacheFilename = addDatasetToSpark(train, s"train-fold-$fold.cache")

      val test = testWithLineNumber.map { case (line, lineNumber) => line }
      val vwTestCacheFilename = addDatasetToSpark(test, s"test-fold-$fold.cache")

      // Add it to the map which will be referenced later on the executor
      map += ((fold, (vwTrainingCacheFilename, vwTestCacheFilename)))
    }.toMap
  }
}

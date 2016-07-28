package com.eharmony.spotz.objective.vw.util

import java.io.{File, PrintWriter}

import com.eharmony.spotz.objective.vw.VwProcess
import com.eharmony.spotz.util.{FileFunctions, FileSystemFunctions, FileUtil, SparkFileFunctions}

import scala.collection.mutable
import scala.io.Source

/**
  * @author vsuthichai
  */
trait FSVwDatasetFunctions extends VwDatasetFunctions with FileSystemFunctions {
  override def getCache(name: String) = get(name)
}

trait SparkVwDatasetFunctions extends VwDatasetFunctions with SparkFileFunctions {
  override def getCache(name: String) = get(name)
}

trait VwDatasetFunctions extends FileFunctions {
  def saveAsCache(inputIterator: Iterator[String]): String = saveAsCache(inputIterator, "dataset.cache")
  def saveAsCache(inputIterable: Iterable[String]): String = saveAsCache(inputIterable.toIterator)
  def saveAsCache(inputPath: String): String = saveAsCache(Source.fromInputStream(FileUtil.loadFile(inputPath)).getLines())

  def saveAsCache(vwDataset: Iterator[String], vwCacheFilename: String): String = {
    // Write VW dataset to a temporary file
    // TODO: Stream this to VW later.
    val vwDatasetFile = FileUtil.tempFile("dataset.vw")
    val vwDatasetWriter = new PrintWriter(vwDatasetFile)
    vwDataset.foreach(line => vwDatasetWriter.println(line))
    vwDatasetWriter.close()
    // vwDatasetFile.delete()

    // Create a VW cache file from the dataset
    val vwCacheFile = FileUtil.tempFile(vwCacheFilename)
    val vwCacheProcess = VwProcess(s"-k --cache_file ${vwCacheFile.getAbsolutePath} -d ${vwDatasetFile.getAbsolutePath}")
    val vwCacheResult = vwCacheProcess()

    assert(vwCacheResult.exitCode == 0,
      s"VW Training cache exited with non-zero exit code ${vwCacheResult.exitCode}")

    save(vwCacheFile)
    vwCacheFile.getName
  }

  def getCache(name: String): File = get(name)
}

trait VwCrossValidation extends VwDatasetFunctions {
  def kFold(inputPath: String, folds: Int): Map[Int, (String, String)] = {
    val enumeratedVwInput = Source.fromInputStream(FileUtil.loadFile(inputPath)).getLines()
    kFold(enumeratedVwInput, folds)
  }

  def kFold(vwDataset: Iterable[String], folds: Int): Map[Int, (String, String)] = {
    kFold(vwDataset.toIterator, folds)
  }

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
    * @param vwDataset
    * @param folds
    * @return a map representation where key is the fold number and value is
    *         (trainingSetFilename, testSetFilename)
    */
  def kFold(vwDataset: Iterator[String], folds: Int): Map[Int, (String, String)] = {
    val enumeratedVwDataset = vwDataset.zipWithIndex.toList

    // For every fold iteration, partition the vw input such that one fold is the test set and the
    // remaining K-1 folds comprise the training set
    (0 until folds).foldLeft(mutable.Map[Int, (String, String)]()) { (map, fold) =>
      val (trainWithLineNumber, testWithLineNumber) = enumeratedVwDataset.partition {
        // train
        case (line, lineNumber) if lineNumber % folds != fold => true
        // test
        case (line, lineNumber) if lineNumber % folds == fold => false
      }

      val train = trainWithLineNumber.map { case (line, lineNumber) => line }.toIterator
      val vwTrainingCacheFilename = saveAsCache(train, s"train-fold-$fold.cache")

      val test = testWithLineNumber.map { case (line, lineNumber) => line }.toIterator
      val vwTestCacheFilename = saveAsCache(test, s"test-fold-$fold.cache")

      // Add it to the map which will be referenced later on the executor
      map + ((fold, (vwTrainingCacheFilename, vwTestCacheFilename)))
    }.toMap
  }
}
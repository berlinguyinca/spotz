package com.eharmony.spotz.objective.vw

import java.io.{File, PrintWriter}

import com.eharmony.spotz.util.FileUtil
import org.apache.spark.{SparkContext, SparkFiles}

import scala.collection.mutable
import scala.io.Source
import scala.language.postfixOps

/**
  * @author vsuthichai
  */
trait BaseVwDatasetFunctions {
  def cacheDataset(inputIterator: Iterator[String]): File = {
    cacheDataset(inputIterator, "dataset.cache")
  }

  def cacheDataset(inputIterable: Iterable[String]): File = {
    cacheDataset(inputIterable.toIterator)
  }

  def cacheDataset(inputPath: String): File = {
    val dataset = Source.fromInputStream(FileUtil.loadFile(inputPath)).getLines()
    cacheDataset(dataset)
  }

  def cacheDataset(vwDataset: Iterator[String], vwCacheFilename: String): File = {
    // Write VW dataset to a temporary file
    // TODO: Stream this to VW later.
    val vwDatasetFile = FileUtil.tempFile("dataset.vw")
    val vwDatasetWriter = new PrintWriter(vwDatasetFile)
    vwDataset.foreach(line => vwDatasetWriter.println(line))
    vwDatasetWriter.close()
    vwDatasetFile.delete()

    // Create a VW cache file from the dataset
    val vwCacheFile = FileUtil.tempFile(vwCacheFilename)
    val vwCacheProcess = VwProcess(s"-k --cache_file ${vwCacheFile.getAbsolutePath} -d ${vwDatasetFile.getAbsolutePath}")
    val vwCacheResult = vwCacheProcess()

    assert(vwCacheResult.exitCode == 0,
      s"VW Training cache exited with non-zero exit code ${vwCacheResult.exitCode}")

    vwCacheFile
  }
}

trait VwDatasetFunctions extends BaseVwDatasetFunctions {
  def saveDataset(vwDataset: Iterator[String]): String
  def getDataset(vwCacheFilename: String): File
}

trait FileSystemVwDatasetFunctions extends VwDatasetFunctions {
  private[this] val cacheFiles = Map[String, String]()

  override def saveDataset(vwDataset: Iterator[String]): String = {
    val vwCacheFile = super.cacheDataset(vwDataset)
    cacheFiles + ((vwCacheFile.getName, vwCacheFile.getAbsolutePath))
    vwCacheFile.getName
  }

  override def getDataset(vwCacheFilename: String): File = {
    new File(cacheFiles(vwCacheFilename))
  }
}

trait SparkVwDatasetFunctions extends VwDatasetFunctions {
  val sparkContext: SparkContext

  override def saveDataset(vwDataset: Iterator[String]): String = {
    val vwCacheFile = super.cacheDataset(vwDataset)
    sparkContext.addFile(vwCacheFile.getAbsolutePath)
    vwCacheFile.getName
  }

  override def getDataset(vwCacheFilename: String): File = {
    new File(SparkFiles.get(vwCacheFilename))
  }
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

      val train = trainWithLineNumber.map { case (line, lineNumber) => line } toIterator
      val vwTrainingCacheFile = cacheDataset(train, s"train-fold-$fold.cache")

      val test = testWithLineNumber.map { case (line, lineNumber) => line } toIterator
      val vwTestCacheFile = cacheDataset(test, s"test-fold-$fold.cache")

      // Add it to the map which will be referenced later on the executor
      map += ((fold, (vwTrainingCacheFile.getName, vwTestCacheFile.getName)))
    }.toMap
  }
}

trait FsVwCrossValidationFunctions extends VwCrossValidation with FileSystemVwDatasetFunctions

trait SparkVwCrossValidationFunctions extends VwCrossValidation with SparkVwDatasetFunctions {
  val sparkContext: SparkContext
}

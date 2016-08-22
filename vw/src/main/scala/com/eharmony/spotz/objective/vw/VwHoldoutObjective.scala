package com.eharmony.spotz.objective.vw

import com.eharmony.spotz.Preamble.Point
import com.eharmony.spotz.objective.Objective
import com.eharmony.spotz.objective.vw.util.{FSVwDatasetFunctions, SparkVwDatasetFunctions, VwDatasetFunctions}
import com.eharmony.spotz.util.{FileUtil, Logging, SparkFileUtil}
import org.apache.spark.SparkContext

/**
  * @author vsuthichai
  */
abstract class AbstractVwHoldoutObjective(
    vwTrainSetPath: String,
    vwTrainParamsString: Option[String],
    vwTestSetPath: String,
    vwTestParamsString: Option[String])
  extends Objective[Point, Double]
  with VwFunctions
  with VwDatasetFunctions with Logging {

  def this(vwTrainSetIterator: Iterator[String],
           vwTrainParamsString: Option[String],
           vwTestSetIterator: Iterator[String],
           vwTestParamsString: Option[String]) = {
    this(VwDatasetFunctions.saveIteratorToDataset(vwTrainSetIterator, "train-dataset.vw").getAbsolutePath, vwTrainParamsString,
         VwDatasetFunctions.saveIteratorToDataset(vwTestSetIterator, "test-dataset.vw").getAbsolutePath, vwTestParamsString)
  }

  def this(vwTrainSetIterable: Iterable[String],
           vwTrainParamsString: Option[String],
           vwTestSetIterable: Iterable[String],
           vwTestParamsString: Option[String]) = {
    this(vwTrainSetIterable.toIterator, vwTrainParamsString, vwTestSetIterable.toIterator, vwTestParamsString)
  }

  val vwTrainParamMap = parseVwArgs(vwTrainParamsString)
  val vwTestParamMap = parseVwArgs(vwTestParamsString)

  val cacheBitSize = getCacheBitSize(vwTrainParamMap)

  val vwTrainCacheFilename = saveAsCache(vwTrainSetPath, "train-dataset.cache", cacheBitSize)
  val vwTestCacheFilename = saveAsCache(vwTestSetPath, "test-dataset.cache", cacheBitSize)

  override def apply(point: Point): Double = {
    // Initialize the model file on the filesystem.  Reserve a unique filename.
    val modelFile = FileUtil.tempFile(s"model.vw")

    // Train
    val vwTrainFile = getCache(vwTrainCacheFilename)
    val vwTrainParams = getTrainVwParams(vwTrainParamMap, point)
    val vwTrainingProcess = VwProcess(s"-f ${modelFile.getAbsolutePath} --cache_file ${vwTrainFile.getAbsolutePath} $vwTrainParams")
    info(s"Executing training: ${vwTrainingProcess.toString}")
    val vwTrainResult = vwTrainingProcess()
    info(s"Train stderr\n${vwTrainResult.stderr}")
    assert(vwTrainResult.exitCode == 0, s"VW Training exited with non-zero exit code s${vwTrainResult.exitCode}")

    // Test
    val vwTestFile = getCache(vwTestCacheFilename)
    val vwTestParams = getTestVwParams(vwTestParamMap, point)
    val vwTestProcess = VwProcess(s"-t -i ${modelFile.getAbsolutePath} --cache_file $vwTestFile $vwTestParams")
    info(s"Executing testing: ${vwTestProcess.toString}")
    val vwTestResult = vwTestProcess()
    assert(vwTestResult.exitCode == 0, s"VW Testing exited with non-zero exit code s${vwTestResult.exitCode}")
    info(s"Test stderr\n${vwTestResult.stderr}")
    val loss = vwTestResult.loss.getOrElse(throw new RuntimeException("Unable to obtain avg loss from test result"))

    // Delete the model.  We don't need these sitting around on the executor's filesystem.
    modelFile.delete()

    loss
  }
}

class SparkVwHoldoutObjective(
    @transient val sc: SparkContext,
    vwTrainSetPath: String,
    vwTrainParamsString: Option[String],
    vwTestSetPath: String,
    vwTestParamsString: Option[String])
  extends AbstractVwHoldoutObjective(vwTrainSetPath, vwTrainParamsString, vwTestSetPath, vwTestParamsString)
  with SparkVwDatasetFunctions
  with Logging {


  def this(sc: SparkContext,
           vwTrainSetIterator: Iterator[String],
           vwTrainParamsString: Option[String],
           vwTestSetIterator: Iterator[String],
           vwTestParamsString: Option[String]) = {
    this(sc, VwDatasetFunctions.saveIteratorToDataset(vwTrainSetIterator, "train-dataset.vw").getAbsolutePath, vwTrainParamsString,
         VwDatasetFunctions.saveIteratorToDataset(vwTestSetIterator, "test-dataset.vw").getAbsolutePath, vwTestParamsString)
  }

  def this(sc: SparkContext,
           vwTrainSetIterable: Iterable[String],
           vwTrainParamsString: Option[String],
           vwTestSetIterable: Iterable[String],
           vwTestParamsString: Option[String]) = {
    this(sc, vwTrainSetIterable.toIterator, vwTrainParamsString, vwTestSetIterable.toIterator, vwTestParamsString)
  }

  /*
  vwTrainSetIterator: Iterator[String],
  vwTrainParamsString: Option[String],
  vwTestSetIterator: Iterator[String],
  vwTestParamsString: Option[String],
  cacheBitSize: Option[Int] = Option(18)


  def this(sc: SparkContext,
           vwTrainSetIterable: Iterable[String],
           vwTrainParamsString: Option[String],
           vwTestSetIterable: Iterable[String],
           vwTestParamsString: Option[String],
           cacheBitSize: Option[Int] = Option(18)) = {
    this(sc, vwTrainSetIterable.toIterator, vwTrainParamsString, vwTestSetIterable.toIterator, vwTestParamsString, cacheBitSize)
  }

  def this(sc: SparkContext,
           vwTrainSetPath: String,
           vwTrainParamsString: Option[String],
           vwTestSetPath: String,
           vwTestParamsString: Option[String],
           cacheBitSize: Option[Int] = Option(18)) = {
    this(sc, SparkFileUtil.loadFile(sc, vwTrainSetPath), vwTrainParamsString, FileUtil.loadFile(vwTestSetPath), vwTestParamsString, cacheBitSize)
  }
  */
}

class VwHoldoutObjective(
    vwTrainSetPath: String,
    vwTrainParamsString: Option[String],
    vwTestSetPath: String,
    vwTestParamsString: Option[String])
  extends AbstractVwHoldoutObjective(vwTrainSetPath, vwTrainParamsString, vwTestSetPath, vwTestParamsString)
    with FSVwDatasetFunctions {


  def this(vwTrainSetIterator: Iterator[String],
           vwTrainParamsString: Option[String],
           vwTestSetIterator: Iterator[String],
           vwTestParamsString: Option[String]) = {
    this(VwDatasetFunctions.saveIteratorToDataset(vwTrainSetIterator, "train-dataset.vw").getAbsolutePath, vwTrainParamsString,
         VwDatasetFunctions.saveIteratorToDataset(vwTestSetIterator, "test-dataset.vw").getAbsolutePath, vwTestParamsString)
  }

  def this(vwTrainSetIterable: Iterable[String],
           vwTrainParamsString: Option[String],
           vwTestSetIterable: Iterable[String],
           vwTestParamsString: Option[String]) = {
    this(vwTrainSetIterable.toIterator, vwTrainParamsString, vwTestSetIterable.toIterator, vwTestParamsString)
  }

/*
  vwTrainSetIterator: Iterator[String],
  vwTrainParamsString: Option[String],
  vwTestSetIterator: Iterator[String],
  vwTestParamsString: Option[String],
  cacheBitSize: Option[Int] = Option(18)

  def this(vwTrainSetIterable: Iterable[String],
           vwTrainParamsString: Option[String],
           vwTestSetIterable: Iterable[String],
           vwTestParamsString: Option[String],
           cacheBitSize: Option[Int] = Option(18)) = {
    this(vwTrainSetIterable.toIterator, vwTrainParamsString, vwTestSetIterable.toIterator, vwTestParamsString, cacheBitSize)
  }

  def this(vwTrainSetPath: String,
           vwTrainParamsString: Option[String],
           vwTestSetPath: String,
           vwTestParamsString: Option[String],
           cacheBitSize: Option[Int] = Option(18)) = {
    this(FileUtil.loadFile(vwTrainSetPath), vwTrainParamsString, FileUtil.loadFile(vwTestSetPath), vwTestParamsString, cacheBitSize)
  }
  */
}

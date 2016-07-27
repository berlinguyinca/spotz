package com.eharmony.spotz.objective.vw

import com.eharmony.spotz.Preamble.Point
import com.eharmony.spotz.objective.Objective
import com.eharmony.spotz.util.{FileUtil, Logger}

import scala.io.Source

/**
  * @author vsuthichai
  */
abstract class VwHoldoutObjective(
    vwTrainSetIterator: Iterator[String],
    vwTrainParamsString: Option[String],
    vwTestSetIterator: Iterator[String],
    vwTestParamsString: Option[String])
  extends Objective[Point, Double]
    with VwFunctions
    with VwDatasetFunctions {

  import VwHoldoutObjective.LOG

  def this(vwTrainSetIterable: Iterable[String],
           vwTrainParamsString: Option[String],
           vwTestSetIterable: Iterable[String],
           vwTestParamsString: Option[String]) = {
    this(vwTrainSetIterable.toIterator, vwTrainParamsString, vwTestSetIterable.toIterator, vwTestParamsString)
  }

  def this(vwTrainSetPath: String,
           vwTrainParamsString: Option[String],
           vwTestSetPath: String,
           vwTestParamsString: Option[String]) = {
    this(Source.fromInputStream(FileUtil.loadFile(vwTrainSetPath)).getLines(), vwTrainParamsString,
         Source.fromInputStream(FileUtil.loadFile(vwTestSetPath)).getLines(),  vwTestParamsString)
  }

  val vwTrainParamMap = parseVwArgs(vwTrainParamsString)
  val vwTestParamMap = parseVwArgs(vwTrainParamsString)

  val vwTrainCacheFilename = saveDataset(vwTrainSetIterator)
  val vwTestCacheFilename = saveDataset(vwTestSetIterator)

  override def apply(point: Point): Double = {
    // Initialize the model file on the filesystem.  Reserve a unique filename.
    val modelFile = FileUtil.tempFile(s"model.vw")

    // Train
    // val vwTrainingFile = SparkFiles.get(vwTrainCacheFile.getName)
    val vwTrainFilename = getDataset(vwTrainCacheFilename)
    val vwTrainParams = getTrainVwParams(vwTrainParamMap, point)
    val vwTrainingProcess = VwProcess(s"-f ${modelFile.getAbsolutePath} --cache_file ${vwTrainFilename.getAbsolutePath} $vwTrainParams")
    LOG.info(s"Executing training: ${vwTrainingProcess.toString}")
    val vwTrainResult = vwTrainingProcess()
    LOG.info(s"Train stderr ${vwTrainResult.stderr}")
    assert(vwTrainResult.exitCode == 0, s"VW Training exited with non-zero exit code s${vwTrainResult.exitCode}")

    // Test
    // val vwTestFile = SparkFiles.get(vwTestCacheFile.getName)
    val vwTestFilename = getDataset(vwTestCacheFilename)
    val vwTestParams = getTestVwParams(vwTestParamMap, point)
    val vwTestProcess = VwProcess(s"-t -i ${modelFile.getAbsolutePath} --cache_file $vwTestFilename $vwTestParams")
    LOG.info(s"Executing testing: ${vwTestProcess.toString}")
    val vwTestResult = vwTestProcess()
    assert(vwTestResult.exitCode == 0, s"VW Testing exited with non-zero exit code s${vwTestResult.exitCode}")
    LOG.info(s"Test stderr ${vwTestResult.stderr}")
    val loss = vwTestResult.loss.getOrElse(throw new RuntimeException("Unable to obtain avg loss from test result"))

    // Delete the model.  We don't need these sitting around on the executor's filesystem.
    modelFile.delete()

    loss
  }
}

object VwHoldoutObjective {
  val LOG = Logger()
}
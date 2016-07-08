package com.eharmony.spotz.objective.vw

import com.eharmony.spotz.Preamble.Point
import com.eharmony.spotz.objective.SparkObjective
import com.eharmony.spotz.util.FileUtil
import org.apache.spark.{SparkContext, SparkFiles}

import scala.io.Source

/**
  * @author vsuthichai
  */
class VwHoldoutObjective(
    @transient val sc: SparkContext,
    vwTrainSetIterator: Iterator[String],
    vwTrainParamsString: Option[String],
    vwTestSetIterator: Iterator[String],
    vwTestParamsString: Option[String])
  extends SparkObjective[Point, Double]
    with VwFunctions
    with VwDatasetLoader {

  def this(sc: SparkContext,
           vwTrainSetIterable: Iterable[String],
           vwTrainParamsString: Option[String],
           vwTestSetIterable: Iterable[String],
           vwTestParamsString: Option[String]) = {
    this(sc, vwTrainSetIterable.toIterator, vwTrainParamsString, vwTestSetIterable.toIterator, vwTestParamsString)
  }

  def this(sc: SparkContext,
           vwTrainSetPath: String,
           vwTrainParamsString: Option[String],
           vwTestSetPath: String,
           vwTestParamsString: Option[String]) = {
    this(sc, Source.fromInputStream(FileUtil.loadFile(vwTrainSetPath)).getLines(), vwTrainParamsString,
             Source.fromInputStream(FileUtil.loadFile(vwTestSetPath)).getLines(),  vwTestParamsString)
  }

  val vwTrainParamMap = parseVwArgs(vwTrainParamsString)
  val vwTestParamMap = parseVwArgs(vwTrainParamsString)

  val vwTrainCacheFilename = prepareVwInput(vwTrainSetIterator)
  val vwTestCacheFilename = prepareVwInput(vwTestSetIterator)

  override def apply(point: Point): Double = {
    // Initialize the model file on the filesystem.  Reserve a unique filename.
    val modelFile = FileUtil.tempFile(s"model.vw")

    // Train
    val vwTrainingFile = SparkFiles.get(vwTrainCacheFilename)
    val vwTrainParams = getTrainVwParams(vwTrainParamMap, point)
    val vwTrainingProcess = VwProcess(s"-f ${modelFile.getAbsolutePath} --cache_file $vwTrainingFile $vwTrainParams")
    logInfo(s"Executing training: ${vwTrainingProcess.toString}")
    val vwTrainResult = vwTrainingProcess()
    logInfo(s"Train stderr ${vwTrainResult.stderr}")
    assert(vwTrainResult.exitCode == 0, s"VW Training exited with non-zero exit code s${vwTrainResult.exitCode}")

    // Test
    val vwTestFile = SparkFiles.get(vwTestCacheFilename)
    val vwTestParams = getTestVwParams(vwTestParamMap, point)
    val vwTestProcess = VwProcess(s"-t -i ${modelFile.getAbsolutePath} --cache_file $vwTestFile $vwTestParams")
    logInfo(s"Executing testing: ${vwTestProcess.toString}")
    val vwTestResult = vwTestProcess()
    assert(vwTestResult.exitCode == 0, s"VW Testing exited with non-zero exit code s${vwTestResult.exitCode}")
    logInfo(s"Test stderr ${vwTestResult.stderr}")
    val loss = vwTestResult.loss.getOrElse(throw new RuntimeException("Unable to obtain avg loss from test result"))

    // Delete the model.  We don't need these sitting around on the executor's filesystem.
    modelFile.delete()

    loss
  }
}

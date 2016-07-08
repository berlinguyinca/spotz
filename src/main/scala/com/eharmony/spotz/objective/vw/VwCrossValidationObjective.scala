package com.eharmony.spotz.objective.vw

import com.eharmony.spotz.Preamble.Point
import com.eharmony.spotz.objective.SparkObjective
import com.eharmony.spotz.util.FileUtil
import org.apache.spark.{SparkContext, SparkFiles}

/**
  *
  * @author vsuthichai
  */
class VwCrossValidationObjective(
    @transient val sc: SparkContext,
    val numFolds: Int,
    val vwDatasetPath: String,
    vwTrainParamsString: Option[String],
    vwTestParamsString: Option[String])
  extends SparkObjective[Point, Double]
    with VwCrossValidationLoader
    with VwFunctions {

  val vwTrainParamsMap = parseVwArgs(vwTrainParamsString)
  val vwTestParamsMap = parseVwArgs(vwTestParamsString)
  val foldToVwCacheFiles = prepareVwInput(vwDatasetPath)

  /**
    * This method can run on the driver and/or the executor.  It performs a k-fold cross validation
    * over the vw input dataset passed through the class constructor.  The dataset has been split in
    * such a way that every fold has its own training and test set in the form of VW cache files.
    *
    * @param point a point object representing the hyper parameters to evaluate upon
    * @return Double the cross validated average loss
    */
  override def apply(point: Point): Double = {
    val vwTrainParams = getTrainVwParams(vwTrainParamsMap, point)
    val vwTestParams = getTestVwParams(vwTestParamsMap, point)

    logInfo(s"Vw Training Params: $vwTrainParams")
    logInfo(s"Vw Testing Params: $vwTestParams")

    val avgLosses = (0 until numFolds).map { fold =>
      // Retrieve the training and test set cache for this fold.
      val (vwTrainingFilename, vwTestFilename) = foldToVwCacheFiles(fold)
      val vwTrainingFile = SparkFiles.get(vwTrainingFilename)
      val vwTestFile = SparkFiles.get(vwTestFilename)

      // Initialize the model file on the filesystem.  Just reserve a unique filename.
      val modelFile = FileUtil.tempFile(s"model-fold-$fold.vw")

      // Train
      val vwTrainingProcess = VwProcess(s"-f ${modelFile.getAbsolutePath} --cache_file $vwTrainingFile $vwTrainParams")
      logInfo(s"Executing training: ${vwTrainingProcess.toString}")
      val vwTrainResult = vwTrainingProcess()
      logInfo(s"Train stderr ${vwTrainResult.stderr}")
      assert(vwTrainResult.exitCode == 0, s"VW Training exited with non-zero exit code s${vwTrainResult.exitCode}")

      // Test
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

    logInfo(s"Avg losses for all folds: $avgLosses")
    val crossValidatedAvgLoss = avgLosses.sum / numFolds
    logInfo(s"Cross validated avg loss: $crossValidatedAvgLoss")

    crossValidatedAvgLoss
  }
}

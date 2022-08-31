package com.eharmony.spotz.examples.vw

import com.eharmony.spotz.examples._
import com.eharmony.spotz.objective.vw.{AbstractVwCrossValidationObjective, SparkVwCrossValidationObjective, VwCrossValidationObjective}
import com.eharmony.spotz.optimizer.hyperparam.{RandomSampler, UniformDouble}
import com.eharmony.spotz.optimizer.StopStrategy
import org.apache.spark.SparkContext

/**
  * @author vsuthichai
  */
trait AbstractVwCrossValidation extends ExampleRunner {
  def getObjective(conf: VwCrossValidationConfiguration): AbstractVwCrossValidationObjective
}

trait VwCrossValidation extends AbstractVwCrossValidation {
  override def getObjective(conf: VwCrossValidationConfiguration): AbstractVwCrossValidationObjective = {
    new VwCrossValidationObjective(
      numFolds = conf.numFolds(),
      vwDatasetPath = conf.datasetPath(),
      vwTrainParamsString = conf.trainParams.toOption,
      vwTestParamsString = conf.testParams.toOption
    )
  }
}

trait SparkVwCrossValidation extends VwCrossValidation {
  val sc: SparkContext

  override def getObjective(conf: VwCrossValidationConfiguration): AbstractVwCrossValidationObjective = {
    new SparkVwCrossValidationObjective(
      sc = sc,
      numFolds = conf.numFolds(),
      vwDatasetPath = conf.datasetPath(),
      vwTrainParamsString = conf.trainParams.toOption,
      vwTestParamsString = conf.testParams.toOption
    )
  }
}

trait VwCrossValidationRandomSearch extends VwCrossValidation {
  val space = Map(
    ("l",  UniformDouble(0, 1)),
    ("l2", UniformDouble(0, 1))
  )

  def getConf(args: Array[String]) = {
    val conf = new VwCrossValidationConfiguration(args) with RandomSearchConfiguration
    conf.verify()
    conf
  }

  def main(args: Array[String]) {
    val conf = new VwCrossValidationConfiguration(args)
    conf.verify()

    val stopStrategy = StopStrategy.stopAfterMaxTrials(conf.numTrials())
    val objective = getObjective(conf)
    val result = apply(objective, space, stopStrategy, conf.numBatchTrials())
    println(result)
  }

  def apply(objective: AbstractVwCrossValidationObjective,
            space: Map[String, RandomSampler[_]],
            stopStrategy: StopStrategy,
            numBatchTrials: Int) = {
    randomSearch(objective, space, stopStrategy, numBatchTrials)
  }
}

trait VwCrossValidationGridSearch extends VwCrossValidation {
  val space = Map(
    ("l",  Range.BigDecimal(0, 1, 0.04).map(_.doubleValue)),
    ("l2", Range.BigDecimal(0, 1, 0.04).map(_.doubleValue))
  )

  def getConf(args: Array[String]) = {
    val conf = new VwCrossValidationConfiguration(args) with GridSearchConfiguration
    conf.verify()
    conf
  }

  def main(args: Array[String]) {
    val conf = getConf(args)
    val objective = getObjective(conf)
    val result = apply(objective, space, conf.numBatchTrials())
    println(result)
  }

  def apply(objective: AbstractVwCrossValidationObjective,
            space: Map[String, Iterable[AnyVal]],
            numBatchTrials: Int) = {
    gridSearch(objective, space, numBatchTrials)
  }
}

object VwCrossValidationSparkGridSearch extends SparkVwCrossValidation with VwCrossValidationGridSearch with SparkExampleRunner
object VwCrossValidationSparkRandomSearch extends SparkVwCrossValidation with VwCrossValidationRandomSearch with SparkExampleRunner
object VwCrossValidationParRandomSearch extends VwCrossValidation with VwCrossValidationRandomSearch with ParExampleRunner
object VwCrossValidationParGridSearch extends VwCrossValidation with VwCrossValidationGridSearch with ParExampleRunner

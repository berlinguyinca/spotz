package com.eharmony.spotz.examples.vw

import com.eharmony.spotz.examples._
import com.eharmony.spotz.objective.vw.{AbstractVwHoldoutObjective, SparkVwHoldoutObjective, VwHoldoutObjective}
import com.eharmony.spotz.optimizer._
import com.eharmony.spotz.optimizer.hyperparam.{Combinations, RandomSampler, UniformDouble}
import org.apache.spark.SparkContext

/**
 * @author vsuthichai
 */
trait AbstractVwHoldout extends ExampleRunner {
  def getObjective(conf: VwHoldoutConfiguration): AbstractVwHoldoutObjective
}

trait SparkVwHoldout extends AbstractVwHoldout {
  val sc: SparkContext

  override def getObjective(conf: VwHoldoutConfiguration): AbstractVwHoldoutObjective = {
    new SparkVwHoldoutObjective(
      sc = sc,
      vwTrainSetPath = conf.trainPath(),
      vwTrainParamsString = conf.trainParams.toOption,
      vwTestSetPath = conf.testPath(),
      vwTestParamsString = conf.testParams.toOption
    )
  }
}

trait VwHoldout extends AbstractVwHoldout {
  override def getObjective(conf: VwHoldoutConfiguration): AbstractVwHoldoutObjective = {
    new VwHoldoutObjective(
      vwTrainSetPath = conf.trainPath(),
      vwTrainParamsString = conf.trainParams.toOption,
      vwTestSetPath = conf.testPath(),
      vwTestParamsString = conf.testParams.toOption
    )
  }
}

trait VwHoldoutRandomSearch extends VwHoldout {
  val space = Map(
    ("l", UniformDouble(0, 1)),
    ("interactions", Combinations('a' to 'z', k = 4, x = 7))
  )

  def main(args: Array[String]) {
    val conf = new VwHoldoutConfiguration(args) with RandomSearchConfiguration
    conf.verify()

    val objective = getObjective(conf)

    val stopStrategy = StopStrategy.stopAfterMaxTrials(conf.numTrials())

    val result = apply(objective, space, stopStrategy, conf.numBatchTrials())
    println(result)
  }

  def apply(objective: AbstractVwHoldoutObjective,
            space: Map[String, RandomSampler[_]],
            stopStrategy: StopStrategy,
            numBatchTrials: Int) = {
    randomSearch(objective, space, stopStrategy, numBatchTrials)
  }
}

trait VwHoldoutGridSearch extends VwHoldout {
  val space = Map(
    ("l", Range.BigDecimal(0, 1, 0.04).map(_.doubleValue)),
    ("l2", Range.BigDecimal(0, 1, 0.04).map(_.doubleValue))
  )

  def main(args: Array[String]) {
    val conf = new VwHoldoutConfiguration(args) with GridSearchConfiguration
    conf.verify()

    val objective = getObjective(conf)

    val result = apply(objective, space, conf.numBatchTrials())
    println(result)
  }

  def apply(objective: AbstractVwHoldoutObjective,
            space: Map[String, Iterable[AnyVal]],
            numBatchTrials: Int) = {
    gridSearch(objective, space, numBatchTrials)
  }
}

object VwHoldoutSparkRandomSearch extends SparkVwHoldout with VwHoldoutRandomSearch with SparkExampleRunner

object VwHoldoutParRandomSearch extends VwHoldout with VwHoldoutRandomSearch with ParExampleRunner

object VwHoldoutSparkGridSearch extends SparkVwHoldout with VwHoldoutGridSearch with SparkExampleRunner

object VwHoldoutParGridSearch extends VwHoldout with VwHoldoutGridSearch with ParExampleRunner

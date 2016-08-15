package com.eharmony.spotz.examples.vw

import com.eharmony.spotz.examples.{ExampleRunner, ParExampleRunner, SparkExampleRunner}
import com.eharmony.spotz.objective.vw.VwHoldoutObjective
import com.eharmony.spotz.optimizer.{RandomSampler, StopStrategy, UniformDouble}
import org.rogach.scallop.ScallopConf

/**
  * @author vsuthichai
  */
class VwHoldoutConfiguration(args: Array[String]) extends ScallopConf(args) {
  val trainPath = opt[String](name = "trainPath", descr = "Absolute path to VW training dataset", required = true)
  val testPath = opt[String](name = "testPath", descr = "Absolute path to VW testing dataset", required = true)
  val trainParams = opt[String](name = "trainParams", descr = "VW training parameters", required = true)
  val testParams = opt[String](name = "testParams", descr = "VW testing parameters", required = false)
  val numTrials = opt[Long](name = "numTrials", descr = "Number of trials", required = true)
  val numSeconds = opt[Int](name = "duration", descr = "Number of seconds", required = true)
  val numBatchTrials = opt[Int](name = "numBatchTrials", descr = "Number of batch trials", short = 'b', default = Option(10), required = false)
}

trait VwHoldout {

}

trait VwHoldoutRandomSearch extends VwHoldout with ExampleRunner {
  val space = Map(
    ("l", UniformDouble(0, 1))
  )

  def main(args: Array[String]) {
    val conf = new VwHoldoutConfiguration(args)
    conf.verify()

    val objective = new VwHoldoutObjective(
      vwTrainSetPath = conf.trainPath(),
      vwTrainParamsString = conf.trainParams.toOption,
      vwTestSetPath = conf.testPath(),
      vwTestParamsString = conf.testParams.toOption
    )

    val stopStrategy = StopStrategy.stopAfterMaxTrials(conf.numTrials())

    val result = apply(objective, space, stopStrategy, conf.numBatchTrials())
    println(result)
  }

  def apply(objective: VwHoldoutObjective,
            space: Map[String, RandomSampler[_]],
            stopStrategy: StopStrategy,
            numBatchTrials: Int) = {
    randomSearch(objective, space, stopStrategy, numBatchTrials)
  }
}

trait VwHoldoutGridSearch extends VwHoldout with ExampleRunner {
  val space = Map(
    ("l",  Range.Double(0, 1, 0.05)),
    ("l2", Range.Double(0, 1, 0.05))
  )

  def main(args: Array[String]) {
    val conf = new VwHoldoutConfiguration(args)
    conf.verify()

    val objective = new VwHoldoutObjective(
      vwTrainSetPath = conf.trainPath(),
      vwTrainParamsString = conf.trainParams.toOption,
      vwTestSetPath = conf.testPath(),
      vwTestParamsString = conf.testParams.toOption
    )

    val result = apply(objective, space, conf.numBatchTrials())
    println(result)
  }

  def apply(objective: VwHoldoutObjective,
            space: Map[String, Iterable[AnyVal]],
            numBatchTrials: Int) = {
    gridSearch(objective, space, numBatchTrials)
  }
}

object VwHoldoutSparkRandomSearch extends VwHoldoutRandomSearch with SparkExampleRunner
object VwHoldoutParRandomSearch extends VwHoldoutRandomSearch with ParExampleRunner
object VwHoldoutSparkGridSearch extends VwHoldoutGridSearch with SparkExampleRunner
object VwHoldoutParGridSearch extends VwHoldoutGridSearch with ParExampleRunner
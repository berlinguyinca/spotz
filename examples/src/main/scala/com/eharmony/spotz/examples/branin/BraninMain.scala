package com.eharmony.spotz.examples.branin

import com.eharmony.spotz.examples.config.{ExampleRunner, ParameterSpaceArgs}
import com.eharmony.spotz.optimizer.{OptimizerConstants, UniformDouble}


/**
  * @author vsuthichai
  */
object BraninMain {
  def paramsForRandomSearch = {
    Map(
      ("x1", new UniformDouble(-5, 10)),
      ("x2", new UniformDouble(0, 15))
    )
  }

  def paramsForGridSearch = {
    Map(
      ("x1", Range.Double(-5, 10, 0.01)),
      ("x2", Range.Double(0, 15, 0.01))
    )
  }

  def params(optimizerName: String) = {
    optimizerName match {
      case OptimizerConstants.RANDOM_SEARCH => ParameterSpaceArgs(forRandomSearch = paramsForRandomSearch)
      case OptimizerConstants.GRID_SEARCH => ParameterSpaceArgs(forGridSearch = paramsForGridSearch)
    }
  }

  def main(args: Array[String]) {
    val conf = new BraninConfiguration(args)
    conf.verify()

    val result = ExampleRunner(conf, new BraninObjective, params(conf.optimizer()), minimize = true)
    println(result)
  }
}


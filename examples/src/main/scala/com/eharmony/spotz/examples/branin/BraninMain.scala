package com.eharmony.spotz.examples.branin

import com.eharmony.spotz.examples.config.{ExampleRunner, ParameterSpaceArgs}
import com.eharmony.spotz.optimizer.UniformDouble
import com.eharmony.spotz.optimizer.OptimizerConstants._


/**
  * @author vsuthichai
  */
object BraninMain extends ExampleRunner {

  def paramSpace(optimizerName: String) = {
    optimizerName match {
      case RANDOM_SEARCH =>
        ParameterSpaceArgs(forRandomSearch = Map(
          ("x1", new UniformDouble(-5, 10)),
          ("x2", new UniformDouble(0, 15))
        ))
      case GRID_SEARCH =>
        ParameterSpaceArgs(forGridSearch = Map(
          ("x1", Range.Double(-5, 10, 0.001)),
          ("x2", Range.Double(0, 15, 0.001))
        ))
    }
  }

  def main(args: Array[String]) {
    val conf = new BraninConfiguration(args)
    conf.verify()

    val result = apply(conf, new BraninObjective, paramSpace(conf.optimizer()), minimize = true)
    println(result)
  }
}


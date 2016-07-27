package com.eharmony.spotz.examples.branin

import com.eharmony.spotz.Preamble._
import com.eharmony.spotz.optimizer.{StopStrategy, UniformDouble}
import com.eharmony.spotz.optimizer.random.{ParRandomSearch, RandomSearch, RandomSpace}

/**
  * @author vsuthichai
  */
object BraninMaxTrials {

  def main(args: Array[String]) {
    val maxTrials = args(0).toInt

    val space = new RandomSpace[Point](Map(
      ("x1", new UniformDouble(-5, 10)),
      ("x2", new UniformDouble(0, 15))
    ))

    val stopStrategy = StopStrategy.stopAfterMaxTrials(maxTrials)
    val optimizer = new ParRandomSearch[Point, Double](stopStrategy)
    val result = optimizer.minimize(new BraninObjective, space)

    println(result)
  }
}

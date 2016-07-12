package com.eharmony.spotz.examples.branin

import com.eharmony.spotz.Preamble._
import com.eharmony.spotz.optimizer.{StopStrategy, UniformDouble}
import com.eharmony.spotz.optimizer.random.{RandomSearch, RandomSpace}
import org.apache.spark.{SparkConf, SparkContext}

/**
  * @author vsuthichai
  */
object BraninMaxTrials {

  def main(args: Array[String]) {
    val maxTrials = args(0).toInt

    val sc = new SparkContext(new SparkConf().setAppName("Branin Max Trials"))

    val space = new RandomSpace[Point](Map(
      ("x1", new UniformDouble(-5, 10)),
      ("x2", new UniformDouble(0, 15))
    ))
    val stopStrategy = StopStrategy.stopAfterMaxTrials(maxTrials)
    val optimizer = new RandomSearch[Point, Double](sc, stopStrategy)
    val result = optimizer.minimize(new BraninObjective, space)

    println(result)
  }
}

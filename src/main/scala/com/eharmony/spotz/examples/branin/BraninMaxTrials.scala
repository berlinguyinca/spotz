package com.eharmony.spotz.examples.branin

import com.eharmony.spotz.Preamble._
import com.eharmony.spotz.optimizer.StopStrategy
import com.eharmony.spotz.optimizer.random.{RandomSearch, RandomSpace, Uniform}
import org.apache.spark.{SparkConf, SparkContext}

import scala.util.Random

/**
  * @author vsuthichai
  */
object BraninMaxTrials {

  def main(args: Array[String]) {
    val maxTrials = args(0).toInt

    val sc = new SparkContext(new SparkConf().setAppName("Branin Max Trials"))

    val seed = Random.nextLong()
    val space = new RandomSpace[Point](seed, Map(
      ("x1", new Uniform(-5, 10)),
      ("x2", new Uniform(0, 15))
    ))
    val stopStrategy = StopStrategy.stopAfterMaxTrials(maxTrials)
    val optimizer = new RandomSearch[Point, Double](sc, stopStrategy)
    val result = optimizer.minimize(new BraninObjective, space)

    println(result)
  }
}

package com.eharmony.spotz.examples.branin

import com.eharmony.spotz.Preamble
import Preamble._
import com.eharmony.spotz.optimizer.RandomSearch
import com.eharmony.spotz.optimizer.stop.StopStrategy
import com.eharmony.spotz.space.{HyperParameter, HyperSpace, Point, Uniform}
import org.apache.spark.{SparkConf, SparkContext}
import org.joda.time.Duration

import scala.util.Random

/**
 * @author vsuthichai
 */
object BraninTimed {

  def main(args: Array[String]) {
    val sc = new SparkContext(new SparkConf().setAppName("Branin Example Timed"))

    val seed = Random.nextLong()

    val space = new HyperSpace(seed, Seq(
      HyperParameter("x1", new Uniform(-5, 10)),
      HyperParameter("x2", new Uniform(0, 15))
    ))

    val seconds = args(0).toInt
    val stopStrategy = StopStrategy.stopAfterMaxDuration(Duration.standardSeconds(seconds))
    val optimizer = new RandomSearch[Point, Double](sc, stopStrategy)
    val result = optimizer.minimize(new BraninObjective, space)

    sc.stop()
    println(result)
  }
}

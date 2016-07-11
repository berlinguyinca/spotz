package com.eharmony.spotz.examples.branin


import com.eharmony.spotz.Preamble._
import com.eharmony.spotz.optimizer.StopStrategy
import com.eharmony.spotz.optimizer.random.{RandomSearch, RandomSpace, Uniform}
import org.apache.spark.{SparkConf, SparkContext}
import org.joda.time.Duration

/**
 * @author vsuthichai
 */
object BraninTimed {

  def main(args: Array[String]) {
    val sc = new SparkContext(new SparkConf().setAppName("Branin Example Timed"))

    val space = new RandomSpace[Point](Map(
      ("x1", new Uniform(-5, 10)),
      ("x2", new Uniform(0, 15))
    ))

    val seconds = args(0).toInt
    val stopStrategy = StopStrategy.stopAfterMaxDuration(Duration.standardSeconds(seconds))
    val optimizer = new RandomSearch[Point, Double](sc, stopStrategy)
    val result = optimizer.minimize(new BraninObjective, space)

    sc.stop()
    println(result)
  }
}

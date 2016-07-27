package com.eharmony.spotz.examples.branin


import com.eharmony.spotz.Preamble._
import com.eharmony.spotz.optimizer.{StopStrategy, UniformDouble}
import com.eharmony.spotz.optimizer.random.{RandomSpace, SparkRandomSearch}
import org.apache.spark.{SparkConf, SparkContext}
import org.joda.time.Duration

/**
 * @author vsuthichai
 */
object BraninTimed {

  def main(args: Array[String]) {
    val sc = new SparkContext(new SparkConf().setAppName("Branin Example Timed"))

    val space = new RandomSpace[Point](Map(
      ("x1", new UniformDouble(-5, 10)),
      ("x2", new UniformDouble(0, 15))
    ))

    val seconds = args(0).toInt
    val stopStrategy = StopStrategy.stopAfterMaxDuration(Duration.standardSeconds(seconds))
    val optimizer = new SparkRandomSearch[Point, Double](sc, stopStrategy)

    val result = optimizer.minimize(new BraninObjective, space)

    sc.stop()
    println(result)
  }
}

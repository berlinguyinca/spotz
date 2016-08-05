package com.eharmony.spotz.examples

import com.eharmony.spotz.Preamble.Point
import com.eharmony.spotz.objective.Objective
import com.eharmony.spotz.optimizer.{OptimizerResult, StopStrategy, UniformDouble}
import org.joda.time.Duration

import scala.math._


/**
  * Input Domain:
  * This function is usually evaluated on the square x1 ∈ [-5, 10], x2 ∈ [0, 15].
  *
  * f(x) = a(x2 - b(x1)^2 + cx1 - r)^2 + s(1 - t)cos(x1) + s
  *
  * Global Minimum:
  * f(x*) = 0.397887 at x* = (-Pi, 12.275), (Pi, 2.275), (9.42478, 2.475)*
  */
class BraninObjective extends Objective[Point, Double] {
  val a = 1
  val b = 5.1 / (4 * pow(Pi, 2))
  val c = 5 / Pi
  val r = 6
  val s = 10
  val t = 1 / (8 * Pi)


  override def apply(point: Point): Double = {
    val x1 = point.get[Double]("x1")
    val x2 = point.get[Double]("x2")

    a * pow(x2 - b*pow(x1, 2) + c*x1 - r, 2) + s*(1-t)*cos(x1) + s
  }
}

trait BraninExample {
  val objective = new BraninObjective
  val stop = StopStrategy.stopAfterMaxDuration(Duration.standardSeconds(5))
  val numBatchTrials = 500000

  def apply(): OptimizerResult[Point, Double]

  def main(args: Array[String]) {
    val result = apply()
    println(result)
  }
}

trait BraninRandomSearch extends BraninExample with RandomSearchRunner with ExampleRunner {
  val hyperParameters = Map(
    ("x1", UniformDouble(-5, 10)),
    ("x2", UniformDouble(0, 15))
  )
}

trait BraninGridSearch extends BraninExample with GridSearchRunner with ExampleRunner {
  val hyperParameters = Map(
    ("x1", Range.Double(-5, 10, 0.01)),
    ("x2", Range.Double(0, 15, 0.01))
  )
}

object BraninParGridSearch extends BraninGridSearch with ParExampleRunner
object BraninParRandomSearch extends BraninRandomSearch with ParExampleRunner
object BraninSparkGridSearch extends BraninGridSearch with SparkExampleRunner
object BraninSparkRandomSearch extends BraninRandomSearch with SparkExampleRunner

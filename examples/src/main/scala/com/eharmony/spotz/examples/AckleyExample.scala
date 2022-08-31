package com.eharmony.spotz.examples

import com.eharmony.spotz.Preamble.Point
import com.eharmony.spotz.objective.Objective
import com.eharmony.spotz.optimizer.hyperparam.UniformDouble
import com.eharmony.spotz.optimizer.{OptimizerResult, StopStrategy}

import scala.math._

/**
 * The Ackley function described here: <link>http://www.sfu.ca/~ssurjano/ackley.html</link>.
 */
class AckleyObjective extends Objective[Point, Double] {
  def apply(p: Point): Double = {
    val x = p.get[Double]("x")
    val y = p.get[Double]("y")

    -20 * exp(-0.2 * sqrt(0.5 * (pow(x, 2) + pow(y, 2)))) - exp(0.5 * (cos(2 * Pi * x) + cos(2 * Pi * y))) + E + 20
  }
}

trait AckleyExample {
  val objective = new AckleyObjective
  val stop = StopStrategy.stopAfterMaxTrials(5000000)
  val numBatchTrials = 500000

  def apply(): OptimizerResult[Point, Double]

  def main(args: Array[String]) {
    val result = apply()
    println(result)
  }
}

trait AckleyRandomSearch extends AckleyExample with RandomSearchRunner {
  val hyperParameters = Map(
    ("x", UniformDouble(-5, 5)),
    ("y", UniformDouble(-5, 5))
  )
}

trait AckleyGridSearch extends AckleyExample with GridSearchRunner {
  val hyperParameters = Map(
    ("x", Range.BigDecimal(-5, 5, 0.01).map(_.doubleValue)),
    ("y", Range.BigDecimal(-5, 5, 0.01).map(_.doubleValue))
  )
}

object AckleyParGridSearch extends AckleyGridSearch with ParExampleRunner

object AckleyParRandomSearch extends AckleyRandomSearch with ParExampleRunner

object AckleySparkGridSearch extends AckleyGridSearch with SparkExampleRunner

object AckleySparkRandomSearch extends AckleyRandomSearch with SparkExampleRunner

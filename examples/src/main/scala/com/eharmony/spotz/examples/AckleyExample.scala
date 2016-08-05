package com.eharmony.spotz.examples

import com.eharmony.spotz.Preamble.Point
import com.eharmony.spotz.objective.Objective
import com.eharmony.spotz.optimizer.grid.GridSearchResult
import com.eharmony.spotz.optimizer.random.RandomSearchResult
import com.eharmony.spotz.optimizer.{OptimizerResult, StopStrategy, UniformDouble}
import org.joda.time.Duration

import scala.math._


/**
  * @author vsuthichai
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
  val stop = StopStrategy.stopAfterMaxDuration(Duration.standardSeconds(5))
  val numBatchTrials = 1000000

  def apply(): OptimizerResult[Point, Double]

  def main(args: Array[String]) {
    val result = apply()
    println(result)
  }
}

trait AckleyRandomSearch extends AckleyExample with ExampleRunner {
  val hyperParameters = Map(
    ("x", UniformDouble(-5, 5)),
    ("y", UniformDouble(-5, 5))
  )

  def apply(): RandomSearchResult[Point, Double] = randomSearch(objective, hyperParameters, stop, numBatchTrials)
}

trait AckleyGridSearch extends AckleyExample with ExampleRunner {
  val hyperParameters = Map(
    ("x", Range.Double(-5, 5, 0.01)),
    ("y", Range.Double(-5, 5, 0.01))
  )

  def apply(): GridSearchResult[Point, Double] = gridSearch(objective, hyperParameters, numBatchTrials)
}

object AckleyParGridSearch extends AckleyGridSearch with ParExampleRunner
object AckleyParRandomSearch extends AckleyRandomSearch with ParExampleRunner
object AckleySparkGridSearch extends AckleyGridSearch with SparkExampleRunner
object AckleySparkRandomSearch extends AckleyRandomSearch with SparkExampleRunner

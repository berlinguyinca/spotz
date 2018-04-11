package com.eharmony.spotz.examples

import com.eharmony.spotz.Preamble.Point
import com.eharmony.spotz.objective.Objective
import com.eharmony.spotz.optimizer.hyperparam.{UniformInt, IndexedChoice, RandomSamplerWithState}
import com.eharmony.spotz.optimizer.{OptimizerResult, StopStrategy}

import scala.math._
import scala.util.Random

class WithStateObjective extends Objective[Point, Double] {
  override def apply(point: Point): Double = point.get[Int]("x1") * 1000 + point.get[Char]("x2").toDouble
}

trait WithStateExample {
  val objective = new WithStateObjective
  val stop = StopStrategy.stopAfterMaxTrials(5000000)
  val numBatchTrials = 500000

  def apply(): OptimizerResult[Point, Double]

  def main(args: Array[String]) {
    val result = apply()
    println(result)
  }
}

trait WithStateRandomSearch extends WithStateExample with RandomSearchRunner with ExampleRunner {
  val hyperParameters = Map(
    ("x1", UniformInt(1, 3)),
    ("x2", IndexedChoice("x1", Map[Int, Vector[Char]](
      (1 -> Vector('g', 'h', 'i')),
      (2 -> Vector('d', 'e', 'f')),
      (3 -> Vector('a', 'b', 'c'))
    )))
  )
}

object WithStateParRandomSearch extends WithStateRandomSearch with ParExampleRunner

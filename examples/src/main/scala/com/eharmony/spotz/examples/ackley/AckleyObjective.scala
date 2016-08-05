package com.eharmony.spotz.examples.ackley

import com.eharmony.spotz.Preamble.Point
import com.eharmony.spotz.objective.Objective

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

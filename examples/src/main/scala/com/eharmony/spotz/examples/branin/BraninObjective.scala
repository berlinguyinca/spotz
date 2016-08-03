package com.eharmony.spotz.examples.branin

import com.eharmony.spotz.Preamble.Point
import com.eharmony.spotz.objective.Objective

import scala.math._

/**
  * @author vsuthichai
  */
class BraninObjective extends Objective[Point, Double] {
  val a = 1
  val b = 5.1 / (4 * pow(Pi, 2))
  val c = 5 / Pi
  val r = 6
  val s = 10
  val t = 1 / (8 * Pi)

  /**
    *  Input Domain:
    *  This function is usually evaluated on the square x1 ∈ [-5, 10], x2 ∈ [0, 15].
    *
    *  Global Minimum:
    *  f(x*) = 0.397887 at x* = (-Pi, 12.275), (Pi, 2.275), (9.42478, 2.475)
    *
    * @param point
    * @return a Double which is the result of evaluating the Branin function
    */
  override def apply(point: Point): Double = {
    val x1 = point.get[Double]("x1")
    val x2 = point.get[Double]("x2")

    a * pow(x2 - b*pow(x1, 2) + c*x1 - r, 2) + s*(1-t)*cos(x1) + s
  }
}


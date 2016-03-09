package com.eharmony.spotz

import com.eharmony.spotz.space.Point

import scala.math.Ordering

/**
 * @author vsuthichai
 */
object Preamble {
  implicit object PointLossOrdering extends Ordering[(Point, Double)] {
    override def compare(x: (Point, Double), y: (Point, Double)): Int = {
      if (x._2 > y._2) 1
      else if (x._2 < y._2) -1
      else 0
    }
  }
}

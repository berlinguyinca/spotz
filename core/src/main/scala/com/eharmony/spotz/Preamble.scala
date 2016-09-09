package com.eharmony.spotz

/**
  * Definitions that define the default implementation of a point of hyper parameter values.
  *
  * @author vsuthichai
  */
object Preamble {
  import scala.language.implicitConversions

  implicit object PointLossOrdering extends Ordering[(Point, Double)] {
    override def compare(x: (Point, Double), y: (Point, Double)): Int = {
      x._2.compareTo(y._2)
    }
  }

  case class Point(hyperParamMap: Map[String, _]) {
    def get[T](label: String): T = hyperParamMap(label).asInstanceOf[T]
    def getHyperParameterLabels: Set[String] = hyperParamMap.keySet
  }

  implicit def pointFactory(params: Map[String, _]): Point = Point(params)
}

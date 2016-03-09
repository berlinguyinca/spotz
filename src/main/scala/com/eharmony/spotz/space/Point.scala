package com.eharmony.spotz.space

/**
 * @author vsuthichai
 */
class Point(private[this] val hyperParameterValues: Map[String, Double]) extends Serializable {

  def get(label: String): Double = hyperParameterValues(label)

  def getHyperParameterLabels: Set[String] = hyperParameterValues.keySet

  override def toString: String = {
    val paramStrings = hyperParameterValues.foldLeft(new StringBuilder()) {
      case (sb, (label, value)) => sb ++= s"$label -> $value, "
    }
    s"Point($paramStrings)"
  }
}

class PointBuilder {
  private[this] val map = scala.collection.mutable.Map[String, Double]()

  def withHyperParameter(label: String, value: Double): PointBuilder = {
    map += ((label, value))
    this
  }

  def build: Point = new Point(map.toMap)
}

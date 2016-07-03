package com.eharmony.spotz.space

import scala.collection.mutable

/**
  * @author vsuthichai
  */
class Point(private[this] val hyperParamMap: Map[String, Any]) extends Serializable {

  def get[T](label: String): T = hyperParamMap(label).asInstanceOf[T]
  def getHyperParameterLabels: Set[String] = hyperParamMap.keySet

  override def toString: String = {
    val paramStrings = hyperParamMap.foldLeft(new StringBuilder()) {
      case (sb, (label, value)) => sb ++= s"$label -> $value, "
    }
    s"Point($paramStrings)"
  }
}

class PointBuilder {

  private[this] val hyperParamMap = mutable.Map[String, Any]()

  def withHyperParameter[T](label: String, value: T): PointBuilder = {
    hyperParamMap.put(label, value)
    this
  }

  def build: Point = new Point(hyperParamMap.toMap)
}

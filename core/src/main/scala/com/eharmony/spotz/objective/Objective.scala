package com.eharmony.spotz.objective

import org.apache.spark.{Logging, SparkContext}

/**
  * @author vsuthichai
  */
trait Objective[P, L] extends Serializable {
  def apply(point: P): L
}

trait SparkObjective[P, L] extends Objective[P, L] with Logging {
  val sparkContext: SparkContext
}

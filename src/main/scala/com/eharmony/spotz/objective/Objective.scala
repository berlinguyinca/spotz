package com.eharmony.spotz.objective

import org.apache.spark.{Logging, SparkContext}

/**
  * @author vsuthichai
  */
trait Objective[P, L] extends Serializable with Logging {
  def apply(point: P): L
}

trait SparkObjective[P, L] extends Objective[P, L] {
  val sc: SparkContext
}

trait CrossValidationObjective[P, L] extends SparkObjective[P, L] {
  def prepareDataset()
}


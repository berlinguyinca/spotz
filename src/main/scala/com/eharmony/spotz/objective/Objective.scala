package com.eharmony.spotz.objective

import org.apache.spark.SparkContext

/**
 * @author vsuthichai
 */
trait Objective[P, L] extends Serializable {
  def apply(point: P): L
}

abstract class SparkObjective[P, L](sc: SparkContext) extends Objective[P, L]

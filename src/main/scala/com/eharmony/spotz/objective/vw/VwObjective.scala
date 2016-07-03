package com.eharmony.spotz.objective.vw

import com.eharmony.spotz.objective.SparkObjective
import org.apache.spark.SparkContext

/**
  * @author vsuthichai
  */
class VwObjective[P, L](@transient val sc: SparkContext) extends SparkObjective[P, L] {
  override def apply(point: P): L = ???
}

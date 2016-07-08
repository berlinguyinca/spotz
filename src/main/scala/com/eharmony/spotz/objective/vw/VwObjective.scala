package com.eharmony.spotz.objective.vw

import com.eharmony.spotz.objective.SparkObjective
import org.apache.spark.SparkContext

/**
  * @author vsuthichai
  */
class VwObjective[P, L](
    @transient val sc: SparkContext,
    vwDatasetPath: String,
    vwProcess: VwProcess)
  extends SparkObjective[P, L]
    with VwFunctions {


  override def apply(point: P): L = ???
}

package com.eharmony.spotz.optimizer.grid

import com.eharmony.spotz.objective.Objective
import com.eharmony.spotz.optimizer._
import org.apache.spark.SparkContext

import scala.math.Ordering

/**
  * @author vsuthichai
  */
class GridSearch[P, L](
    @transient val sc: SparkContext)
    (implicit val ord: Ordering[(P, L)])
  extends SparkBaseOptimizer[P, L, GridSpace[P], GridSearchResult[P, L]] {

  override def optimize(objective: Objective[P, L],
                        space: GridSpace[P],
                        reducer: Reducer[(P, L)]): GridSearchResult[P, L] = ???
}

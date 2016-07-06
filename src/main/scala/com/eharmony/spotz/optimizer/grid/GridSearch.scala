package com.eharmony.spotz.optimizer.grid

import com.eharmony.spotz.objective.Objective
import com.eharmony.spotz.optimizer._
import com.eharmony.spotz.space.Space
import com.eharmony.spotz.spark.SparkFunctions
import org.apache.spark.SparkContext

import scala.math.Ordering

/**
  * @author vsuthichai
  */
class GridSearch[P, L](
    @transient sc: SparkContext)
    (implicit pointLossOrdering: Ordering[(P, L)])
  extends SparkBaseOptimizer[P, L](sc)(pointLossOrdering)
    with SparkFunctions[P, L] {

  override def optimize(objective: Objective[P, L],
                        space: Space[P],
                        reducer: Reducer[(P, L)]): GridSearchResult[P, L] = ???
}

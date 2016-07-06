package com.eharmony.spotz.optimizer

import com.eharmony.spotz.objective.Objective
import com.eharmony.spotz.optimizer.random.RandomSearchResult
import com.eharmony.spotz.space.Space
import org.apache.spark.SparkContext

import scala.math.Ordering

/**
 * @author vsuthichai
 */
trait Optimizer[P, L] extends Serializable {
  def minimize(objective: Objective[P, L], space: Space[P]): OptimizerResult[P, L]
  def maximize(objective: Objective[P, L], space: Space[P]): OptimizerResult[P, L]
}

abstract class SparkBaseOptimizer[P, L](@transient val sc: SparkContext)(implicit ord: Ordering[(P, L)]) extends Optimizer[P, L] {
  def min(p1: (P,L), p2: (P,L))(implicit ord: Ordering[(P,L)]): (P,L) = ord.min(p1, p2)
  def max(p1: (P,L), p2: (P,L))(implicit ord: Ordering[(P,L)]): (P,L) = ord.max(p1, p2)

  override def minimize(objective: Objective[P, L], space: Space[P]): OptimizerResult[P, L] = {
    optimize(objective, space, min)
  }

  override def maximize(objective: Objective[P, L], space: Space[P]): OptimizerResult[P, L] = {
    optimize(objective, space, max)
  }

  def optimize(objective: Objective[P, L], space: Space[P], reducer: Reducer[(P, L)]): OptimizerResult[P, L]
}

class OptimizerResult[P, L](best: P, bestLoss: L)

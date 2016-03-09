package com.eharmony.spotz.optimizer

import com.eharmony.spotz.objective.Objective
import com.eharmony.spotz.space.Space

import scala.math.Ordering

/**
 * @author vsuthichai
 */
trait Optimizer[P, L] extends Serializable {
  def minimize(objective: Objective[P, L], space: Space[P]): OptimizerResult[P, L]
  def maximize(objective: Objective[P, L], space: Space[P]): OptimizerResult[P, L]
}

abstract class BaseOptimizer[P, L] extends Optimizer[P, L] {
  def min(p1: (P,L), p2: (P,L))(implicit ord: Ordering[(P,L)]): (P,L) = ord.min(p1, p2)
  def max(p1: (P,L), p2: (P,L))(implicit ord: Ordering[(P,L)]): (P,L) = ord.max(p1, p2)
}

class OptimizerResult[P, L](best: P, bestLoss: L)

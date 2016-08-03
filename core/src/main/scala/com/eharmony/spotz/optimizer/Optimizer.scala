package com.eharmony.spotz.optimizer

import com.eharmony.spotz.objective.Objective

import scala.math.Ordering
import scala.reflect.ClassTag

/**
  * @author vsuthichai
  */
trait Optimizer[P, L, +R] extends Serializable {
  def minimize(objective: Objective[P, L])(implicit c: ClassTag[P], p: ClassTag[L]): R
  def maximize(objective: Objective[P, L])(implicit c: ClassTag[P], p: ClassTag[L]): R
}

/*
trait AbstractOptimizer[P, L, -S <: Space[P], +R <: OptimizerResult[P, L]] extends Optimizer[P, L, S, R] {
  type Reducer[T] = (T, T) => T
  implicit val ord: Ordering[(P, L)]

  protected def min(p1: (P,L), p2: (P,L))(implicit ord: Ordering[(P,L)]): (P,L) = ord.min(p1, p2)
  protected def max(p1: (P,L), p2: (P,L))(implicit ord: Ordering[(P,L)]): (P,L) = ord.max(p1, p2)
  protected def optimize(objective: Objective[P, L], space: S, reducer: Reducer[(P, L)])
                        (implicit c: ClassTag[P], p: ClassTag[L]): R

  override def minimize(objective: Objective[P, L], space: S)(implicit c: ClassTag[P], p: ClassTag[L]): R = {
    optimize(objective, space, min)
  }

  override def maximize(objective: Objective[P, L], space: S)(implicit c: ClassTag[P], p: ClassTag[L]): R = {
    optimize(objective, space, max)
  }
}
*/

trait AbstractOptimizer[P, L, R <: OptimizerResult[P, L]] extends Optimizer[P, L, R] {
  type Reducer[T] = (T, T) => T
  implicit val ord: Ordering[(P, L)]

  protected def min(p1: (P,L), p2: (P,L))(implicit ord: Ordering[(P,L)]): (P,L) = ord.min(p1, p2)
  protected def max(p1: (P,L), p2: (P,L))(implicit ord: Ordering[(P,L)]): (P,L) = ord.max(p1, p2)
  protected def optimize(objective: Objective[P, L], reducer: Reducer[(P, L)])
                        (implicit c: ClassTag[P], p: ClassTag[L]): R

  override def minimize(objective: Objective[P, L])(implicit c: ClassTag[P], p: ClassTag[L]): R = {
    optimize(objective, min)
  }

  override def maximize(objective: Objective[P, L])(implicit c: ClassTag[P], p: ClassTag[L]): R = {
    optimize(objective, max)
  }
}

abstract class OptimizerResult[P, L](bestPoint: P, bestLoss: L)

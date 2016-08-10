package com.eharmony.spotz.optimizer

import com.eharmony.spotz.objective.Objective

import scala.math.Ordering
import scala.reflect.ClassTag

/**
  * Optimizer trait.
  *
  * @tparam P point type passed to objective function
  * @tparam L loss returned from objective function evaluation
  * @tparam R optimizer result containing the best point and minimized or maximized loss
  */
trait Optimizer[P, L, S, +R] extends Serializable {
  def minimize(objective: Objective[P, L], space: S)(implicit c: ClassTag[P], p: ClassTag[L]): R
  def maximize(objective: Objective[P, L], space: S)(implicit c: ClassTag[P], p: ClassTag[L]): R
}


trait AbstractOptimizer[P, L, S, R <: OptimizerResult[P, L]] extends Optimizer[P, L, S, R] {
  type Reducer[T] = (T, T) => T

  /** The ordering of a point, loss tuple used by the reducer */
  implicit val ord: Ordering[(P, L)]

  protected def min(p1: (P,L), p2: (P,L))(implicit ord: Ordering[(P,L)]): (P,L) = ord.min(p1, p2)
  protected def max(p1: (P,L), p2: (P,L))(implicit ord: Ordering[(P,L)]): (P,L) = ord.max(p1, p2)

  override def minimize(objective: Objective[P, L], space: S)(implicit c: ClassTag[P], p: ClassTag[L]): R = {
    optimize(objective, space, min)
  }

  override def maximize(objective: Objective[P, L], space: S)(implicit c: ClassTag[P], p: ClassTag[L]): R = {
    optimize(objective, space, max)
  }

  /**
    * Concrete subclasses of this trait should override this method to provide an optimization
    * implementation.
    *
    * @param objective the objective function
    * @param space the hyperparameter space to search
    * @param reducer function to reduce (P, L) tuples
    * @param c point type P class tag
    * @param p loss type L class tag
    * @return an OptimizerResult[P, L]
    */
  protected def optimize(objective: Objective[P, L], space: S, reducer: Reducer[(P, L)])
                        (implicit c: ClassTag[P], p: ClassTag[L]): R
}

abstract class OptimizerResult[P, L](bestPoint: P, bestLoss: L)

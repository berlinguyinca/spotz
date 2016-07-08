package com.eharmony.spotz.optimizer

import com.eharmony.spotz.objective.Objective
import org.apache.spark.SparkContext

import scala.math.Ordering

/**
 * @author vsuthichai
 */
trait Optimizer[P, L, -S <: Space[P], +R <: OptimizerResult[P, L]] extends Serializable {
  def minimize(objective: Objective[P, L], space: S): R
  def maximize(objective: Objective[P, L], space: S): R
}

trait BaseOptimizer[P, L, -S <: Space[P], +R <: OptimizerResult[P, L]] extends Optimizer[P, L, S, R] {
  type Reducer[T] = (T, T) => T
  implicit val ord: Ordering[(P, L)]

  def min(p1: (P,L), p2: (P,L))(implicit ord: Ordering[(P,L)]): (P,L) = ord.min(p1, p2)
  def max(p1: (P,L), p2: (P,L))(implicit ord: Ordering[(P,L)]): (P,L) = ord.max(p1, p2)

  override def minimize(objective: Objective[P, L], space: S): R = {
    optimize(objective, space, min)
  }

  override def maximize(objective: Objective[P, L], space: S): R = {
    optimize(objective, space, max)
  }

  protected def optimize(objective: Objective[P, L], space: S, reducer: Reducer[(P, L)]): R
}

trait SparkBaseOptimizer[P, L, -S <: Space[P], +R <: OptimizerResult[P, L]] extends BaseOptimizer[P, L, S, R] {
  val sc: SparkContext
}

abstract class OptimizerResult[P, L](bestPoint: P, bestLoss: L)

trait Space[P] extends Serializable {
  def sample: P
  def sample(howMany: Int): Iterable[P] = Seq.fill(howMany)(sample)
}

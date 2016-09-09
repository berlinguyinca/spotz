package com.eharmony.spotz.optimizer.hyperparam

import scala.util.Random

/**
  * Created by vsuthichai on 8/18/16.
  */
abstract class Uniform[T](lb: T, ub: T) extends RandomSampler[T]

/**
  * Sample a Double within the bounds lb <= x < ub with uniform random distribution
  *
  * {{{
  *   val rng = new Random(seed)
  *   val sampler = UniformDouble(0, 1))
  *   val sample = sampler(rng)
  * }}}
  *
  * @param lb lower bound
  * @param ub upper bound
  */
case class UniformDouble(lb: Double, ub: Double) extends Uniform[Double](lb, ub) {
  require(lb < ub, "lb must be less than ub")

  override def apply(rng: Random): Double = lb + ((ub - lb) * rng.nextDouble)
}

/**
  * Sample an Int within the bounds lb <= x < ub with uniform random distribution
  *
  * {{{
  *   val hyperParamSpace = Map(
  *     ("x1", UniformInt(0, 10))
  *   )
  * }}}
  *
  * @param lb lower bound
  * @param ub upper bound
  */
case class UniformInt(lb: Int, ub: Int) extends Uniform[Int](lb, ub) {
  require(lb < ub, "lb must be less than ub")

  override def apply(rng: Random): Int = lb + rng.nextInt(ub - lb)
}

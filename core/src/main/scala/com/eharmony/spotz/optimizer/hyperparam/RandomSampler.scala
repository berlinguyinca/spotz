package com.eharmony.spotz.optimizer.hyperparam

import scala.util.Random

/**
  * A sampler function dependent on a pseudo random number generator.  The generator
  * is passed in as a parameter, and this is intentional.  It allows the user to
  * change seeds and switch generators.  This becomes important when sampling on
  * Spark workers and more control over the rng is necessary.
  *
  * @tparam T
  */
trait RandomSampler[T] extends Serializable {
  def apply(rng: Random): T
}

trait RandomSamplerWithState[T] extends RandomSampler[T] {
  def apply(rng: Random, params: Map[String, _]): T
  def apply(rng: Random) = apply(rng, Map.empty[String, Any])
}

trait CombinatoricRandomSampler[T] extends RandomSampler[Iterable[Iterable[T]]]
trait IterableRandomSampler[T] extends RandomSampler[Iterable[T]]

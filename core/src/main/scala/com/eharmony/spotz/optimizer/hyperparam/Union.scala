package com.eharmony.spotz.optimizer.hyperparam

import scala.util.Random

/**
  * Given an iterable of RandomSampler functions, choose a function at random and
  * sample from it.
  *
  * {{{
  *   val hyperParamSpace = Map(
  *     ("x1", Union(UniformDouble(0, 1), UniformDouble(10, 11)))
  *   )
  * }}}
  *
  * @param iterable an iterable of RandomSampler[T] functions
  * @param probs an iterable of probabilities that should sum to 1.  This specifies the probabilities that
  *              the sampler functions are chosen.  If the length of this is not the same as the length
  *              of the iterable of RandomSampler[T] functions, then an IllegalArgumentException is thrown.
  *              Not specifying an iterable of probabilities will force a default uniform random sampling.
  *              If the length of the iterable of probabilities is equal to the length of the iterable of
  *              RandomSampler[T] functions, then
  * @tparam T type parameter of the sample
  */
case class Union[T](iterable: Iterable[RandomSampler[T]], probs: Iterable[Double] = Seq()) extends RandomSampler[T] {
  private val indexedSeq = iterable.toIndexedSeq

  private val probabilities =
    if (probs.isEmpty)
      Seq.fill(indexedSeq.length)(1.toDouble / indexedSeq.length)
    else if (probs.toIndexedSeq.length != indexedSeq.length)
      throw new IllegalArgumentException("iterable lengths must match")
    else if (probs.exists(p => p <= 0))
      throw new IllegalArgumentException("Must be positive or valid probabilities")
    else if (probs.sum != 1.0)
      probs.map(p => p / probs.sum)

  override def apply(rng: Random): T = ???
  private def bucket(probability: Double): Int = ???
}

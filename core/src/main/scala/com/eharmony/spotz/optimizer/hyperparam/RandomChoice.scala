package com.eharmony.spotz.optimizer.hyperparam

import scala.util.Random

/**
  * Sample an element from an Iterable of fixed length with uniform random distribution.
  *
  * {{{
  *   val hyperParamSpace = Map(
  *     ("x1", RandomChoice(Seq("svm", "logistic")))
  *   )
  * }}}
  *
  * @param iterable an iterable of type T
  * @tparam T type parameter of iterable
  */
case class RandomChoice[T](iterable: Iterable[T]) extends RandomSampler[T] {
  private val values = iterable.toIndexedSeq

  require(values.length > 1, "Empty iterable")

  override def apply(rng: Random): T = values(rng.nextInt(values.length))
}

package com.eharmony.spotz.optimizer.hyperparam

import scala.collection.mutable
import scala.util.Random

/**
  *
  * @param iterable
  * @param k
  * @param replacement
  * @param ord
  * @tparam T
  */
abstract class AbstractSubset[T](iterable: Iterable[T], k: Int, replacement: Boolean = false)(implicit ord: Ordering[T]) extends Serializable {
  protected val values = iterable.toIndexedSeq

  def sample(rng: Random): Iterable[T] = {
    val sampleSize = rng.nextInt(k) + 1
    val subset = mutable.SortedSet[T]()
    val indices = mutable.Set[Int]()

    while (subset.size < sampleSize) {
      val index = rng.nextInt(values.size)

      if (replacement) {
        subset.add(values(index))
      } else if (!indices.contains(index)) {
        indices.add(index)
        subset.add(values(index))
      }
    }

    subset.toIndexedSeq
  }
}

/**
  *
  * @param iterable
  * @param k
  * @param replacement
  * @param ord
  * @tparam T
  */
case class Subset[T](
    iterable: Iterable[T],
    k: Int,
    replacement: Boolean = false)
    (implicit ord: Ordering[T])
  extends AbstractSubset[T](iterable, k, replacement)
  with IterableRandomSampler[T] {

  assert(k > 0 && k <= values.size, "K must be in the interval (0, N]")

  def apply(rng: Random): Iterable[T] = sample(rng)
}

/**
  *
  * @param iterable
  * @param k
  * @param x
  * @param replacement
  * @param ord
  * @tparam T
  */
case class Subsets[T](
    iterable: Iterable[T],
    k: Int,
    x: Int,
    replacement: Boolean = false)
    (implicit ord: Ordering[T])
  extends AbstractSubset[T](iterable, k, replacement)
  with CombinatoricRandomSampler[T] {

  assert(k > 0 && k <= values.size, "K must be in the interval (0, N]")
  assert(x > 0, "X must be greater than 0")

  /**
    *
    * @param rng
    * @return
    */
  def apply(rng: Random): Iterable[Iterable[T]] = {
    val numSubsets = rng.nextInt(x) + 1

    if (replacement) {
      Seq.fill(numSubsets)(sample(rng))
    } else {
      val subsets = mutable.Set[Iterable[T]]()
      while (subsets.size < numSubsets) {
        subsets.add(sample(rng))
      }
      subsets.toIndexedSeq
    }
  }
}



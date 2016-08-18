package com.eharmony.spotz.optimizer.hyperparam

import scala.collection.mutable
import scala.util.Random

/**
  * Sample a subset of size up to K from an iterable of length N, with or without replacement
  *
  * @param iterable an iterable of length N
  * @param K number of items sampled, up to K, chosen randomly between 1 to K
  * @param replacement boolean indicating sample with or without replacement
  * @param ord ordering of type T
  * @tparam T element type of iterable
  */
abstract class AbstractSubset[T](
    iterable: Iterable[T],
    K: Int,
    replacement: Boolean = false)
    (implicit ord: Ordering[T])
  extends Serializable {

  protected val values = iterable.toIndexedSeq

  protected def sampleWithReplacement(rng: Random): Iterable[T] = {
    val sampleSize = rng.nextInt(K) + 1
    val subset = new mutable.PriorityQueue[T]()

    while (subset.size < K) {
      val index = rng.nextInt(values.length)
      subset += values(index)
    }

    subset.toIndexedSeq
  }

  protected def sampleNoReplacement(rng: Random): Iterable[T] = {
    val sampleSize = rng.nextInt(K) + 1
    val subset = mutable.SortedSet[T]()
    val indices = mutable.Set[Int]()

    while (subset.size < sampleSize) {
      val index = rng.nextInt(values.size)

      if (!indices.contains(index)) {
        indices.add(index)
        subset.add(values(index))
      }
    }

    subset.toIndexedSeq
  }

  protected def sample(rng: Random): Iterable[T] = {
    if (replacement) sampleWithReplacement(rng)
    else sampleNoReplacement(rng)
  }
}

/**
  * Sample a single subset.
  *
  * @param iterable an iterable of length N
  * @param K number of items sampled, up to K, chosen randomly between 1 to K
  * @param replacement boolean indicating sample with or without replacement
  * @param ord ordering of type T
  * @tparam T element type of iterable
  */
case class Subset[T](
    iterable: Iterable[T],
    K: Int,
    replacement: Boolean = false)
    (implicit ord: Ordering[T])
  extends AbstractSubset[T](iterable, K, replacement)
  with IterableRandomSampler[T] {

  assert(K > 0 && K <= values.size, "K must be in the interval (0, N]")

  def apply(rng: Random): Iterable[T] = sample(rng)
}

/**
  * Sample many subsets, up to X subsets.
  *
  * @param iterable an iterable of length N
  * @param K number of items sampled, up to K, chosen randomly between 1 to K
  * @param X number of subsets to sample
  * @param replacement boolean indicating sample with or without replacement
  * @param ord ordering of type T
  * @tparam T element type of iterable
  */
case class Subsets[T](
    iterable: Iterable[T],
    K: Int,
    X: Int,
    replacement: Boolean = false)
    (implicit ord: Ordering[T])
  extends AbstractSubset[T](iterable, K, replacement)
  with CombinatoricRandomSampler[T] {

  assert(K > 0 && K <= values.size, "K must be in the interval (0, N]")
  assert(X > 0, "X must be greater than 0")

  def apply(rng: Random): Iterable[Iterable[T]] = {
    val numSubsets = rng.nextInt(X) + 1

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



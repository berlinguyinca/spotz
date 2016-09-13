package com.eharmony.spotz.optimizer.hyperparam

import scala.collection.mutable
import scala.util.Random

/**
  * Sample with or without replacement a combination of K items from an iterable
  * of length N.  The combination returned will never be an empty set.  Samples
  * returned are in lexicographical order.
  *
  * @param iterable an iterable of type T
  * @param k number of items to choose from the iterable
  * @param replacement boolean indicating sample with or without replacement
  * @param ord Ordering of type T
  * @tparam T element type of iterable
  */
abstract class AbstractCombinations[T](
    iterable: Iterable[T],
    k: Int,
    replacement: Boolean = false)(implicit ord: Ordering[T]) extends Serializable {

  protected val values = iterable.toSeq

  protected def sample(rng: Random): Iterable[T] = {
    if (replacement) sampleWithReplacement(rng)
    else sampleNoReplacement(rng)
  }

  protected def sampleWithReplacement(rng: Random): IndexedSeq[T] = {
    val combo = new mutable.PriorityQueue[T]()

    while (combo.size < k) {
      val index = rng.nextInt(values.length)
      combo += values(index)
    }

    combo.toIndexedSeq
  }

  protected def sampleNoReplacement(rng: Random): IndexedSeq[T] = {
    val combo = mutable.SortedSet[T]()

    while (combo.size < k) {
      val index = rng.nextInt(values.length)
      combo.add(values(index))
    }

    combo.toIndexedSeq
  }
}

/**
  * Sample a single combination of K items from the iterable of length N.
  *
  * @param iterable an iterable of type T
  * @param k the number of items to sample from the iterable of length N
  * @param replacement boolean indicating whether to sample with replacement
  * @param ord Ordering of type T
  * @tparam T element type of iterable
  */
case class Combination[T](
    iterable: Iterable[T],
    k: Int,
    replacement: Boolean = false)(implicit ord: Ordering[T])
  extends AbstractCombinations[T](iterable, k, replacement)(ord) with IterableRandomSampler[T] {

  require(k > 0, "k must be greater than 0")
  require(k <= values.length, s"k must be less than or equal to length of the iterable, ${values.length}")

  override def apply(rng: Random): Iterable[T] = sample(rng)
}


/**
  * Binomial coefficient implementation.  Choose K items from an Iterable of N items.
  * Also known as N Choose K, where N is the size of an Iterable and K is the desired number
  * of items to be chosen.  Items will always be returned in lexicographical order.
  *
  * @param iterable an iterable of finite length
  * @param k the number of items to sample from the iterable of length N to form a combination
  * @param x number of combinations to sample
  * @param replacement boolean indicating whether to sample with replacement
  * @param ord Ordering of type T
  * @tparam T element type of iterable
  */
case class Combinations[T](
    iterable: Iterable[T],
    k: Int,
    x: Int = 1,
    replacement: Boolean = false)(implicit ord: Ordering[T])
  extends AbstractCombinations[T](iterable, k, replacement)(ord) with CombinatoricRandomSampler[T] {

  require(k > 0, "k must be greater than 0")
  require(k <= values.length, s"k must be less than or equal to length of the iterable, ${values.length}")

  override def apply(rng: Random): Iterable[Iterable[T]] = {
    if (replacement) {
      Seq.fill(x)(sample(rng))
    } else {
      val numElements = x // scala.math.min(x, combinations.size)
      val ret = collection.mutable.Set[Iterable[T]]()
      while (ret.size < numElements) {
        ret += sample(rng)
      }
      ret.toIndexedSeq
    }
  }
}

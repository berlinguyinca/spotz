package com.eharmony.spotz.optimizer.hyperparam

import scala.collection.mutable
import scala.util.Random

/**
  *
  * @param iterable
  * @param k
  * @param replacement
  * @tparam T
  */
abstract class AbstractCombinations[T](
    iterable: Iterable[T],
    k: Int,
    replacement: Boolean = false)(implicit ord: Ordering[T]) extends Serializable {

  protected val values = iterable.toSeq

  assert(k > 0, "k must be greater than 0")
  assert(k <= values.length, s"k must be less than or equal to length of the iterable, ${values.length}")

  def sample(rng: Random): Iterable[T] = {
    if (replacement) sampleWithReplacement(rng)
    else sampleNoReplacement(rng)
  }

  def sampleWithReplacement(rng: Random) = {
    val combo = mutable.SortedSet[T]()

    while (combo.size < k) {
      val index = rng.nextInt(values.length)
      val element = values(rng.nextInt(values.length))
      combo.add(element)
    }

    combo.toSeq
  }

  def sampleNoReplacement(rng: Random) = {
    val combo = mutable.SortedSet[T]()
    val indices = mutable.Set[Int]()

    while (combo.size < k) {
      val index = rng.nextInt(values.length)
      if (!indices.contains(index)) {
        indices.add(index)
        combo.add(values(index))
      }
    }

    combo.toSeq
  }
}

/**
  * Sample a single combination of K unordered items from the iterable of length N.
  *
  * @param iterable
  * @param k
  * @param replacement
  * @tparam T
  */
case class Combination[T](
    iterable: Iterable[T],
    k: Int,
    replacement: Boolean = false)(implicit ord: Ordering[T])
  extends AbstractCombinations[T](iterable, k, replacement)(ord) with IterableRandomSampler[T] {

  assert(k > 0, "k must be greater than 0")
  assert(k <= values.length, s"k must be less than or equal to length of the iterable, ${values.length}")

  override def apply(rng: Random): Iterable[T] = sample(rng)
}


/**
  * Binomial coefficient implementation.  Pick K unordered items from an Iterable of N items.
  * Also known as N Choose K, where N is the size of an Iterable and K is the desired number
  * of items to be chosen.  This implementation will actually compute all the possible choices
  * and return them as an Iterable.
  *
  * @param iterable an iterable of finite length
  * @param k the number of items to choose
  * @tparam T
  */
case class Combinations[T](
    iterable: Iterable[T],
    k: Int,
    x: Int = 1,
    replacement: Boolean = false)(implicit ord: Ordering[T])
  extends AbstractCombinations[T](iterable, k, replacement)(ord) with CombinatoricRandomSampler[T] {

  assert(k > 0, "k must be greater than 0")
  assert(k <= values.length, s"k must be less than or equal to length of the iterable, ${values.length}")

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

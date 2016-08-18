package com.eharmony.spotz.optimizer.hyperparam

import scala.collection.mutable
import scala.util.Random

case class Subset[T](iterable: Iterable[T], k: Int)


case class Subsets[T](iterable: Iterable[T], k: Int, x: Int, replacement: Boolean = false)(implicit ord: Ordering[T]) extends CombinatoricRandomSampler[T] {
  private val values = iterable.toIndexedSeq

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



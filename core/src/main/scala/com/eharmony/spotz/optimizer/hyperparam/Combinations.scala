package com.eharmony.spotz.optimizer.hyperparam

import scala.util.Random


trait CombinatoricRandomSampler[T] extends RandomSampler[Iterable[Iterable[T]]]
trait IterableRandomSampler[T] extends RandomSampler[Iterable[T]]

/**
  *
  * @param iterable
  * @param k
  * @param x
  * @param replacement
  * @tparam T
  */
abstract class AbstractCombinations[T](
    iterable: Iterable[T],
    k: Int,
    x: Int = 1,
    replacement: Boolean = false) extends Serializable {

  import org.paukov.combinatorics3.Generator

  import scala.collection.JavaConverters._

  private val values = iterable.toSeq

  assert(k > 0, "k must be greater than 0")
  assert(k <= values.length, s"k must be less than or equal to length of the iterable, ${values.length}")

  // TODO: This is hideous!  Rewrite this to be more memory efficient by unranking combinations.  For now, use a Java lib.
  val combinations = Generator.combination(iterable.asJavaCollection).simple(k).asScala.toIndexedSeq.map(l => l.asScala.toIndexedSeq)

  /**
    *
    * @param rng
    * @return
    */
  def combos(rng: Random): Iterable[Iterable[T]] = {
    if (replacement) {
      Seq.fill(x)(combinations(rng.nextInt(combinations.size)))
    } else {
      val indices = collection.mutable.Set[Int]()
      val numElements = scala.math.min(x, combinations.size)
      val ret = new collection.mutable.ArrayBuffer[Iterable[T]](numElements)
      while (indices.size < numElements) {
        val index = rng.nextInt(combinations.size)
        if (!indices.contains(index)) {
          indices.add(index)
          ret += combinations(index)
        }
      }
      ret.toIndexedSeq
    }
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
                           replacement: Boolean = false)
  extends AbstractCombinations[T](iterable, k, 1, replacement) with IterableRandomSampler[T] {

  override def apply(rng: Random): Iterable[T] = combos(rng).head
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
    replacement: Boolean = false)
  extends AbstractCombinations[T](iterable, k, x, replacement) with CombinatoricRandomSampler[T] {

  override def apply(rng: Random): Iterable[Iterable[T]] = combos(rng)
}

package com.eharmony.spotz.optimizer

import scala.util.Random

/**
  * @author vsuthichai
  */
trait SamplerFunction[T] extends Serializable

/**
  * A sampler function dependent on a pseudo random number generator.  The generator
  * is passed in as a parameter, and this is intentional.  It allows the user to
  * change seeds and switch generators.  This becomes important when sampling on
  * Spark workers and more control over the rng is necessary.
  *
  * @tparam T
  */
abstract class RandomSampler[T] extends SamplerFunction[T] {
  def apply(rng: Random): T
}

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
  if (lb >= ub)
    throw new IllegalArgumentException("lb must be less than ub")

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
  if (lb >= ub)
    throw new IllegalArgumentException("lb must be less than ub")

  override def apply(rng: Random): Int = lb + rng.nextInt(ub - lb)
}

/**
  * Sample from a normal distribution given the mean and standard deviation
  *
  * {{{
  *   val hyperParamSpace = Map(
  *     ("x1", NormalDistribution(0, 0.1))
  *   )
  * }}}
  *
  * @param mean mean
  * @param std standard deviation
  */
case class NormalDistribution(mean: Double, std: Double) extends RandomSampler[Double] {
  override def apply(rng: Random): Double = {
    std * rng.nextGaussian() + mean
  }
}

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

  if (values.length < 1)
    throw new IllegalArgumentException("Empty iterable")

  override def apply(rng: Random): T = values(rng.nextInt(values.length))
}

/**
  * N Choose K, where N is the size of an Iterable.
  *
  * @param iterable
  * @param k
  * @tparam T
  */
case class BinomialCoefficient[T](iterable: Iterable[T], k: Int) extends RandomSampler[Iterable[T]] {
  private val values = iterable.toSeq

  override def apply(rng: Random): Iterable[T] = {
    rng.shuffle(values).take(k)
  }
}

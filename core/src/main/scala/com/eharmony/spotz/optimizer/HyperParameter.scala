package com.eharmony.spotz.optimizer

import scala.util.Random

/**
  * @author vsuthichai
  */
trait SamplerFunction[T] extends Serializable

abstract class RandomSampler[T] extends SamplerFunction[T] {
  def apply(rng: Random): T
}

abstract class Uniform[T](lb: T, ub: T) extends RandomSampler[T]

/**
  * Sample a Double within the bounds with uniform random distribution
  *
  * lb <= x < ub
  *
  * @param lb
  * @param ub
  */
case class UniformDouble(lb: Double, ub: Double) extends Uniform[Double](lb, ub) {
  if (lb >= ub)
    throw new IllegalArgumentException("lb must be less than ub")

  override def apply(rng: Random): Double = lb + ((ub - lb) * rng.nextDouble)
}

/**
  * Sample an Int within the bounds with uniform random distribution
  *
  * lb <= x < ub
  *
  * @param lb
  * @param ub
  */
case class UniformInt(lb: Int, ub: Int) extends Uniform[Int](lb, ub) {
  if (lb >= ub)
    throw new IllegalArgumentException("lb must be less than ub")

  override def apply(rng: Random): Int = lb + rng.nextInt(ub - lb)
}

/**
  * Sample from a normal distribution given the mean and standard deviation
  *
  * @param mean
  * @param std
  */
case class NormalDistribution(mean: Double, std: Double) extends RandomSampler[Double] {
  override def apply(rng: Random): Double = {
    std * rng.nextGaussian() + mean
  }
}

/**
  * Sample uniform random from an Iterable.
  *
  * @param iterable
  * @tparam T
  */
case class RandomChoice[T](iterable: Iterable[T]) extends RandomSampler[T] {
  private val values = iterable.toIndexedSeq

  if (values.length < 1)
    throw new IllegalArgumentException("Empty iterable")

  override def apply(rng: Random): T = values(rng.nextInt(values.length))
}

/**
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

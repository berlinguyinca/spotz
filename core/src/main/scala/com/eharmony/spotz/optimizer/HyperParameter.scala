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

case class UniformDouble(lb: Double, ub: Double) extends Uniform[Double](lb, ub) {
  if (lb >= ub)
    throw new IllegalArgumentException("lb must be less than ub")

  override def apply(rng: Random): Double = lb + ((ub - lb) * rng.nextDouble)
}

case class UniformInt(lb: Int, ub: Int) extends Uniform[Int](lb, ub) {
  if (lb >= ub)
    throw new IllegalArgumentException("lb must be less than ub")

  override def apply(rng: Random): Int = lb + rng.nextInt(ub - lb)
}

case class NormalDistribution(mean: Double, std: Double) extends RandomSampler[Double] {
  override def apply(rng: Random): Double = {
    std * rng.nextGaussian() + mean
  }
}

case class RandomChoice[T](iterable: Iterable[T]) extends RandomSampler[T] {
  val values = iterable.toIndexedSeq

  if (values.length < 1)
    throw new IllegalArgumentException("Empty iterable")

  override def apply(rng: Random): T = values(rng.nextInt(values.length))
}

case class BinomialCoefficient[T](iterable: Iterable[T], k: Int) extends RandomSampler[Iterable[T]] {
  val values = iterable.toSeq

  override def apply(rng: Random): Iterable[T] = {
    rng.shuffle(values).take(k)
  }
}

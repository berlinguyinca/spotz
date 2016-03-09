package com.eharmony.spotz.space

import scala.util.Random

/**
 * @author vsuthichai
 */
case class HyperParameter(label: String, samplingFunction: SamplingFunction[Double]) {
  def sample(implicit rng: Random) = samplingFunction.sample
}

trait SamplingFunction[T] extends Serializable {
  def sample(implicit rng: Random): T
}

abstract class RangeSamplingFunction(
    lowerBound: Double,
    upperBound: Double)
  extends SamplingFunction[Double]

class Uniform(
    lowerBound: Double,
    upperBound: Double)
  extends RangeSamplingFunction(lowerBound, upperBound) {

  override def sample(implicit rng: Random): Double = lowerBound + ((upperBound - lowerBound) * rng.nextDouble)
}

class RandInt(
    lowerBound: Int,
    upperBound: Int,
    seed: Long = 0)
  extends RangeSamplingFunction(lowerBound, upperBound) {

  override def sample(implicit rng: Random): Double = lowerBound + rng.nextInt(upperBound - lowerBound)
}

class FromSeq[T](seq: Seq[T], seed: Long = 0) extends SamplingFunction[T] {
  override def sample(implicit rng: Random): T = seq(rng.nextInt(seq.length))
}

class Choice[T](choices: Seq[SamplingFunction[T]]) extends SamplingFunction[T] {
  override def sample(implicit rng: Random): T = choices(rng.nextInt(choices.length)).sample
}

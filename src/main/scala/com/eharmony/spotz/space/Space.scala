package com.eharmony.spotz.space

import scala.util.Random

/**
 * @author vsuthichai
 */
trait Space[T] extends Serializable {
  def sample: T
  def sample(howMany: Int): Iterable[T] = Seq.fill(howMany)(sample)
  def seed(seed: Long): Space[T]
}

// Scala 2.10 Random is not Serializable?
class SerializableRandom(seed: Long) extends Random(seed) with Serializable

case class HyperSpace(seed: Long, params: Seq[HyperParameter[_]]) extends Space[Point] {
  val rng = new SerializableRandom(seed)
  rng.nextDouble()  // Get rid of the first value to avoid low entropy in the seed

  override def sample: Point = {
    params.foldLeft(new PointBuilder()) { (pb, param) =>
      pb.withHyperParameter(param.label, param.sample(rng))
    }.build
  }

  override def seed(newSeed: Long): Space[Point] = {
    this.copy(params = this.params, seed = newSeed)
  }
}

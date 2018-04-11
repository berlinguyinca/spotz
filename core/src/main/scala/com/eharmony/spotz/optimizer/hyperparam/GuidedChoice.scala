package com.eharmony.spotz.optimizer.hyperparam

import scala.util.Random

abstract class GuidedChoice[K, T](key: String) extends RandomSamplerWithState[T] {
  protected def guide(m: Map[String, Any]): K = m(key).asInstanceOf[K]
}

case class IndexedChoice[K, T](key: String, values: Map[K, Vector[T]]) extends GuidedChoice[K, T](key) {
  private val choices = values.mapValues(xs => RandomChoice(xs))

  def apply(rng: Random, m: Map[String, Any]): T = choices(guide(m))(rng)
}

case class TransformedChoice[K, T](key: String, transform: K => T) extends GuidedChoice[K, T](key) {
  def apply(rng: Random, m: Map[String, Any]): T = transform(guide(m))
}

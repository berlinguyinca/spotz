package com.eharmony.spotz.optimizer

/**
  * @author vsuthichai
  */
trait Space[P] extends Serializable {
  def sample: P
  def sample(howMany: Int): Iterable[P] = Seq.fill(howMany)(sample)
}

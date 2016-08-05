package com.eharmony.spotz.optimizer

import scala.util.Random

/**
  * @author vsuthichai
  */
trait RandomTest {
  val seed = new Random().nextLong
  val rng = new Random(seed)
}

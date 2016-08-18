package com.eharmony.spotz.optimizer

import com.eharmony.spotz.optimizer.hyperparam.RandomChoice
import org.junit.Assert._
import org.junit.Test

/**
  * @author vsuthichai
  */
class RandomChoiceTest extends RandomTest {
  @Test
  def testRandomChoice() {
    val rc = RandomChoice(Seq(1,2,3))
    val seq = Seq.fill(1000000)(rc(rng))
    val relFreqOnes = seq.count(i => i == 1).toDouble / seq.size
    val relFreqTwos = seq.count(i => i == 1).toDouble / seq.size
    val relFreqThrees = seq.count(i => i == 1).toDouble / seq.size

    assertEquals(relFreqOnes, 0.333, 0.01)
    assertEquals(relFreqTwos, 0.333, 0.01)
    assertEquals(relFreqThrees, 0.333, 0.01)
  }

  @Test(expected = classOf[IllegalArgumentException])
  def testEmptyChoices() {
    val rc = RandomChoice(Seq())
  }
}

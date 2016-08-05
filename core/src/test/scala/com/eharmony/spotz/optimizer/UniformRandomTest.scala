package com.eharmony.spotz.optimizer

import org.junit.Assert._
import org.junit.Test

import scala.math._

/**
  * @author vsuthichai
  */
class UniformRandomTest extends RandomTest {
  val lb = 0
  val ub = 1
  val uniformRandomSampler = new UniformDouble(lb, ub)
  val numSamples = 2000000
  val samples = Seq.fill(numSamples)(uniformRandomSampler(rng))
  val mean = samples.sum / numSamples
  val std = sqrt(samples.map(x => pow(x - mean, 2)).sum / (numSamples - 1))

  @Test
  def testUniformRandomMean() {
    assertEquals(mean, (lb + ub) / 2.toDouble, 0.001)
  }

  @Test
  def testUniformRandomStd() {
    assertEquals(std, (ub - lb) / sqrt(12), 0.001)
  }
}

package com.eharmony.spotz.examples.branin

import org.junit.Assert._
import org.junit.Test

/**
  * @author vsuthichai
  */
class BraninTest {
  @Test
  def testBraninParRandomSearch() {
    val result = BraninParRandomSearch()
    assertEquals(result.bestLoss, 0.397887, 0.001)
  }

  @Test
  def testBraninParGridSearch() {
    val result = BraninParGridSearch()
    assertEquals(result.bestLoss, 0.397887, 0.001)
  }

  @Test
  def testBraninSparkRandomSearch() {
    val result = BraninSparkRandomSearch()
    assertEquals(result.bestLoss, 0.397887, 0.001)
  }

  @Test
  def testBraninSparkGridSearch() {
    val result = BraninSparkGridSearch()
    assertEquals(result.bestLoss, 0.397887, 0.001)
  }
}

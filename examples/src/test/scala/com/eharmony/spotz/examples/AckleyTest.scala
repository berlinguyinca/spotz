package com.eharmony.spotz.examples

import org.junit.Assert._
import org.junit.{Ignore, Test}

/**
  * @author vsuthichai
  */
class AckleyTest {
  @Test
  def testAckleyParRandomSearch() {
    val result = AckleyParRandomSearch()
    val point = result.bestPoint

    assertEquals(result.bestLoss, 0.0, 0.003)
    assertEquals(point.get[Double]("x"), 0.0, 0.001)
    assertEquals(point.get[Double]("y"), 0.0, 0.001)
  }

  @Test
  def testAckleyParGridSearch() {
    val result = AckleyParGridSearch()
    val point = result.bestPoint

    assertEquals(result.bestLoss, 0.0, 0.003)
    assertEquals(point.get[Double]("x"), 0.0, 0.001)
    assertEquals(point.get[Double]("y"), 0.0, 0.001)
  }

  @Test
  def testAckleySparkRandomSearch() {
    val result = AckleySparkRandomSearch()
    val point = result.bestPoint

    assertEquals(result.bestLoss, 0.0, 0.003)
    assertEquals(point.get[Double]("x"), 0.0, 0.001)
    assertEquals(point.get[Double]("y"), 0.0, 0.001)
  }

  @Test
  def testAckleySparkGridSearch() {
    val result = AckleySparkGridSearch()
    val point = result.bestPoint

    assertEquals(result.bestLoss, 0.0, 0.003)
    assertEquals(point.get[Double]("x"), 0.0, 0.001)
    assertEquals(point.get[Double]("y"), 0.0, 0.001)
  }
}


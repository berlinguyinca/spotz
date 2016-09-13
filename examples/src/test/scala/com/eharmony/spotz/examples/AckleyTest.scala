package com.eharmony.spotz.examples

import com.eharmony.spotz.Preamble.Point
import org.junit.Assert._
import org.junit.Test

/**
  * @author vsuthichai
  */
class AckleyTest {
  @Test
  def testAckleyParRandomSearch() {
    val result = AckleyParRandomSearch()
    checkAssertions(result.bestPoint, result.bestLoss)
  }

  @Test
  def testAckleyParGridSearch() {
    val result = AckleyParGridSearch()
    checkAssertions(result.bestPoint, result.bestLoss)
  }

  @Test
  def testAckleySparkRandomSearch() {
    val result = AckleySparkRandomSearch()
    checkAssertions(result.bestPoint, result.bestLoss)
  }

  @Test
  def testAckleySparkGridSearch() {
    val result = AckleySparkGridSearch()
    checkAssertions(result.bestPoint, result.bestLoss)
  }

  def checkAssertions(p: Point, l: Double) {
    assertEquals(l, 0.0, 0.01)
    assertEquals(0.0, p.get[Double]("x"), 0.01)
    assertEquals(0.0, p.get[Double]("y"), 0.01)
  }
}


package com.eharmony.spotz.examples

import com.eharmony.spotz.Preamble.Point
import org.junit.Assert._
import org.junit.Test

import scala.math.Pi

/**
  * @author vsuthichai
  */
class BraninTest {
  @Test
  def testBraninParRandomSearch() {
    val result = BraninParRandomSearch()
    checkAssertions(result.bestPoint, result.bestLoss)
  }

  @Test
  def testBraninParGridSearch() {
    val result = BraninParGridSearch()
    checkAssertions(result.bestPoint, result.bestLoss)
  }

  @Test
  def testBraninSparkRandomSearch() {
    val result = BraninSparkRandomSearch()
    checkAssertions(result.bestPoint, result.bestLoss)
  }

  @Test
  def testBraninSparkGridSearch() {
    val result = BraninSparkGridSearch()
    checkAssertions(result.bestPoint, result.bestLoss)
  }

  def checkAssertions(p: Point, l: Double) {
    assertEquals(0.397887, l, 0.001)

    try {
      checkSolution1(p)
    } catch {
      case _: AssertionError =>
        try {
          checkSolution2(p)
        } catch {
          case _: AssertionError => checkSolution3(p)
        }
    }
  }

  def checkSolution1(p: Point) {
    assertEquals(-Pi, p.get[Double]("x1"), 0.01)
    assertEquals(12.275, p.get[Double]("x2"), 0.01)
  }

  def checkSolution2(p: Point) {
    assertEquals(Pi, p.get[Double]("x1"), 0.01)
    assertEquals(2.275, p.get[Double]("x2"), 0.01)
  }

  def checkSolution3(p: Point) {
    assertEquals(9.42478, p.get[Double]("x1"), 0.01)
    assertEquals(2.475, p.get[Double]("x2"), 0.01)
  }
}

package com.eharmony.spotz.examples

import com.eharmony.spotz.Preamble.Point
import org.junit.Assert._
import org.junit.Test

import scala.math.Pi

class WithStateTest {
  @Test
  def testBraninParRandomSearch() {
    val result = WithStateParRandomSearch()
    checkAssertions(result.bestPoint, result.bestLoss)
  }

  def checkAssertions(p: Point, l: Double) {
    val i = (l / 1000).toInt
    val d = l % 1000
    val c = d.asInstanceOf[Char]
    val map = Map[Int, Vector[Char]](
      (1 -> Vector('g', 'h', 'i')),
      (2 -> Vector('d', 'e', 'f')),
      (3 -> Vector('a', 'b', 'c'))
    )
    assertTrue(s"Value $i does not correspond to char $c ($d)", map(i).contains(c))
  }
}

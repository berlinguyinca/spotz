package com.eharmony.spotz.optimizer.grid

import com.eharmony.spotz.Preamble.Point

import org.junit.Assert._
import org.junit.Test


/**
  * @author vsuthichai
  */
class GridSpaceTest {
  val simpleGrid = new GridSpace[Point](Map(
    ("foo", Range.Double(0, 1, 0.001)),
    ("bar", Range.Double(0, 1, 0.001))
  ))

  @Test
  def testGrid() {
    val gs = new GridSpace[Point](Map(
      ("foo", Range.Double(0, 1, 0.001)),
      ("bar", Range.Double(0, 1, 0.001))
    ))

    assertEquals(gs.length, 1000 * 1000)
    assertEquals(gs(0), new Point(Map(("foo", 0.0), ("bar", 0.0))))
    assertEquals(gs(gs.length - 1), new Point(Map(("foo", 0.999), ("bar", 0.999))))
  }

  @Test
  def testBigGrid() {
    val gs = new GridSpace[Point](Map(
      ("foo", Range.Double(0, 1, 0.000001)),
      ("bar", Range.Double(0, 1, 0.000001)),
      ("baz", Range.Double(0, 1, 0.000001))
    ))

    assertEquals(gs.length, 1000000L * 1000000L * 1000000L)
    assertEquals(gs(0), new Point(Map(("foo", 0.0), ("bar", 0.0), ("baz", 0.0))))
    assertEquals(gs(gs.length - 1), new Point(Map(("foo", 0.999999), ("bar", 0.999999), ("baz", 0.999999))))
  }

  @Test(expected = classOf[IndexOutOfBoundsException])
  def testIndexOutOfBoundsAfterEnd() {
    val point = simpleGrid(simpleGrid.length)
  }

  @Test(expected = classOf[IndexOutOfBoundsException])
  def testIndexOutOfBoundsBeforeStart() {
    val point = simpleGrid(-1)
  }
}

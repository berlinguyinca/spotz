package com.eharmony.spotz.optimizer.grid

import com.eharmony.spotz.util.Logging

import scala.collection.mutable.ArrayBuffer
import scala.language.postfixOps

/**
  * This grid implementation computes hyper parameter values in an iterative manner.  The entire
  * cartesian product space of the grid is not ballooned into memory, but is instead computed
  * on demand through the apply method.
  *
  * The algorithm details are documented here: <link>http://phrogz.net/lazy-cartesian-product</code>
  *
  * Accessing elements of the grid is similar to accessing elements inside an IndexedSeq.
  *
  * {{{
  *   import com.eharmony.spotz.Preamble._
  *
  *   val grid = new GridSpace(Map(
  *     ("x1", Range.Double(0.0, 1.0, 0.1)),
  *     ("x2", Range.Double(0.0, 1.0, 0.1))
  *   )
  *
  *   val firstElement = grid(0)          // Map(x1 -> 0.0, x2 -> 0.0)
  *   val lastElement = grid(99)          // Map(x1 -> 1.0, x2 -> 1.0)
  *   val lastElementPlusOne = grid(100)  // IndexOutOfBoundsException
  * }}}
  *
  * Given the factory function which defines the Map transformation, a point object P of the caller's
  * implementation can be instantiated: (Map[String, _]) => P.  This is passed implicitly.
  *
  * The <code>GridSearch</code> algorithm will use this class to iterate through all the grid element
  * points and pass them for evaluation to the objective function.
  *
  * @author vsuthichai
  */
class Grid[P](
    gridParams: Map[String, Iterable[_]])
    (implicit factory: (Map[String, _]) => P)
  extends Serializable
  with Logging {

  assert(gridParams.nonEmpty, "No grid parameters have been specified")

  /** Expand the each grid row.  The memory required is linear in the sum of lengths of all the iterables */
  /** pre-compute the divisible factor and store along with the length inside GridProperty */
  private val gridRows = gridParams.map { case (label, it) => (label, it.toSeq) }
    .foldRight(ArrayBuffer[GridRow]()) { case ((label, it), b) =>
      if (b.isEmpty) GridRow(label, it, 1L, it.length.toLong) +=: b
      else GridRow(label, it, b.head.length * b.head.factor, it.length.toLong) +=: b
    }.toIndexedSeq

  val length = gridRows.foldLeft(1L)((product, gridRow) => product * gridRow.length)
  val size = length

  info(s"$size hyper parameter tuples found in GridSpace")

  /**
    *
    * @param idx
    * @return
    */
  def apply(idx: Long): P = {
    if (idx < 0 || idx >= size)
      throw new IndexOutOfBoundsException(idx.toString)

    val gridRowIndices = gridRows.map(gridRow => (idx / gridRow.factor) % gridRow.length)

    val hyperParamValues = gridRowIndices.zipWithIndex.map { case (columnIndex, rowIndex) =>
      (gridRows(rowIndex).label, gridRows(rowIndex).values(columnIndex.toInt))
    }.toMap

    factory(hyperParamValues)
  }
}

/**
  * This case class contains the label name and the values of any specific row within the grid.
  *
  * @param label
  * @param values
  * @param factor
  * @param length
  */
case class GridRow(label: String, values: Seq[_], factor: Long, length: Long)

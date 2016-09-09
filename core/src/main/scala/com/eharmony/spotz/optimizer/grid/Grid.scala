package com.eharmony.spotz.optimizer.grid

import com.eharmony.spotz.util.Logging

import scala.collection.mutable.ArrayBuffer
import scala.language.postfixOps

/**
  * This grid implementation computes hyper parameter values using the algorithm found here:
  *
  * <link>http://phrogz.net/lazy-cartesian-product</code>
  *
  * The entire cartesian product space of the grid is not ballooned into memory, but is
  * instead computed on demand through the apply method.
  *
  * Grid elements are accessible through an index, similarly to an IndexedSeq.
  *
  * {{{
  *   import com.eharmony.spotz.Preamble._
  *
  *   val grid = new Grid[Point](Map(
  *     ("x1", Range.Double(0.0, 1.0, 0.1)),
  *     ("x2", Range.Double(0.0, 1.0, 0.1))
  *   )
  *
  *   val length = grid.length            // 100
  *   val firstElement = grid(0)          // Point(Map(x1 -> 0.0, x2 -> 0.0))
  *   val lastElement = grid(99)          // Point(Map(x1 -> 1.0, x2 -> 1.0))
  *   val lastElementPlusOne = grid(100)  // IndexOutOfBoundsException
  * }}}
  *
  * The factory function defines the transformation from the Map of sampled hyper parameters
  * to the point P, which is passed implicitly.
  *
  * The <code>GridSearch</code> algorithm will use this class to iterate through all the grid element
  * points by index and pass them for evaluation to the objective function.
  *
  * @author vsuthichai
  */
class Grid[P](gridParams: Map[String, Iterable[_]])
             (implicit factory: (Map[String, _]) => P) extends Serializable with Logging {

  require(gridParams.nonEmpty, "No grid parameters have been specified")

  private lazy val gridRows = computeGrid()
  lazy val length = computeLength()
  lazy val size = length

  /**
    * Expand each grid row.  The memory required is linear in the sum of lengths of all
    * the iterables.  Pre-compute the divisible factor and store along with the length inside
    * GridRow.
    */
  private def computeGrid(): IndexedSeq[GridRow] = {
    val expandedGridParams = gridParams.map { case (label, it) => (label, it.toSeq) }

    val gridRowProperties = expandedGridParams.foldRight(ArrayBuffer[GridRow]()) { case ((label, it), buf) =>
      if (buf.isEmpty) GridRow(label, it, 1L, it.length.toLong) +=: buf
      else GridRow(label, it, buf.head.length * buf.head.divisor, it.length.toLong) +=: buf
    }

    gridRowProperties.toIndexedSeq
  }

  private def computeLength(): Long = {
    val len = gridRows.foldLeft(1L)((product, gridRow) => product * gridRow.length)
    info(s"$len hyper parameter tuples found in Grid space")
    len
  }

  /**
    * Retrieve an indexed element from this grid.
    *
    * @param idx a zero-based index into the grid
    * @return a point type P
    */
  def apply(idx: Long): P = {
    if (idx < 0 || idx >= size)
      throw new IndexOutOfBoundsException(idx.toString)

    // Compute the column indices for every row
    val gridColumnIndices = gridRows.map(gridRow => (idx / gridRow.divisor) % gridRow.length)

    // Look up the hyper parameter values in grid
    val hyperParamValues = gridColumnIndices.zipWithIndex.map { case (columnIndex, rowIndex) =>
      (gridRows(rowIndex).label, gridRows(rowIndex).values(columnIndex.toInt))
    }.toMap

    // Build a point object
    factory(hyperParamValues)
  }
}

/**
  * This case class contains the label name, values, and other properties of a specific
  * row within the grid.
  *
  * @param label the hyper paramater label for this row in the grid
  * @param values the iterable values for this row in the grid
  * @param divisor the divisor used when computing the column index within this row: (index / divisor) % length
  * @param length the length of this row
  */
case class GridRow(label: String, values: Seq[_], divisor: Long, length: Long)

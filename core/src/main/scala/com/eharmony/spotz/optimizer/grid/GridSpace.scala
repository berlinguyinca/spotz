package com.eharmony.spotz.optimizer.grid

import com.eharmony.spotz.util.Logging

import scala.collection.mutable.ArrayBuffer
import scala.language.postfixOps

/**
  * This grid implementation computes hyper parameter values in an iterative manner.  The entire
  * cartesian product space of the grid is not ballooned into memory, but is instead computed
  * iteratively on demand through the sample method.  This is intended to be used in conjunction
  * with <code>GridSearch</code> in a single thread on the driver and not on the executors.
  * Thusly, it is not thread safe.
  *
  * @author vsuthichai
  */
class GridSpace[P](gridParams: Map[String, Iterable[_]])
                  (implicit factory: (Map[String, _]) => P) extends Serializable with Logging {

  assert(gridParams.nonEmpty, "No grid parameters have been specified")

  // Expand the entire grid space.  The memory required is linear in the sum of lengths of all the iterables
  private val gridSpace = gridParams.map { case (label, it) => (label, it.toSeq) } toSeq

  // pre-compute the length of each grid iterable, indexed into a Seq
  private val gridLengths = gridSpace.map { case (label, seq) => seq.length.toLong }

  // pre-compute the divisible factor and store along with the length inside GridProperty
  // http://phrogz.net/lazy-cartesian-product
  private val gridProperties = gridLengths.foldRight(ArrayBuffer[GridProperty]()) { case (l, b) =>
    if (b.isEmpty) GridProperty(1L, l) +=: b
    else GridProperty(b.head.length * b.head.factor, l) +=: b
  }

  val max = gridLengths.product

  info(s"$max hyper parameter tuples found in GridSpace")

  var i: Long = 0

  def length: Long = max

  def apply(idx: Long): P = {
    if (idx < 0 || idx >= max)
      throw new IndexOutOfBoundsException(idx.toString)

    val gridIndices = gridProperties.map { case GridProperty(factor, length) => (idx / factor) % length }
    val hyperParamValues = gridIndices.zipWithIndex.map { case (columnIndex, rowIndex) =>
      (gridSpace(rowIndex)._1, gridSpace(rowIndex)._2(columnIndex.toInt))
    }.toMap

    factory(hyperParamValues)
  }
/*
  override def hasNext: Boolean = i < max

  override def next(): P = {
    if (!hasNext)
      throw new NoSuchElementException("Grid space has been exhausted of all values.")

    val point = apply(i)

    i += 1

    point
  }
  */
}

case class GridRow(label: String, values: Seq[_])
case class GridProperty(factor: Long, length: Long)
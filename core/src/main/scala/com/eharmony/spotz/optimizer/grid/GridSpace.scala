package com.eharmony.spotz.optimizer.grid

import com.eharmony.spotz.optimizer.Space
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
                  (implicit factory: (Map[String, _]) => P) extends Space[P] with Logging {

  assert(gridParams.nonEmpty, "No grid parameters have been specified")

  // Expand the entire grid space.  The memory required is linear in the sum of lengths of all the iterables
  private[this] val gridSpace = gridParams.map { case (label, it) => (label, it.toSeq) } toSeq

  // pre-compute the length of each grid iterable, indexed into a Seq
  private[this] val gridLengths = gridSpace.map { case (label, seq) => seq.length }

  // pre-compute the divisible factor and store along with the length inside GridProperty
  // http://phrogz.net/lazy-cartesian-product
  private[this] val gridProperties = gridLengths.foldRight(ArrayBuffer[GridProperty]()) { case (l: Int, b) =>
    if (b.isEmpty) GridProperty(1L, l) +=: b
    else GridProperty(b.head.length * b.head.factor, l) +=: b
  }

  val max = gridLengths.product.toLong

  info(s"$max hyper parameter tuples found in GridSpace")

  var i: Long = 0

  override def sample: P = {
    if (isExhausted)
      throw new NoSuchElementException("Grid space has been exhausted of all values.")

    val gridIndices = gridProperties.map { case GridProperty(factor, length) => (i / factor) % length }
    val hyperParamValues = gridIndices.zipWithIndex.map { case (columnIndex, rowIndex) =>
      (gridSpace(rowIndex)._1, gridSpace(rowIndex)._2(columnIndex.toInt))
    }.toMap

    i += 1

    factory(hyperParamValues)
  }

  override def sample(howMany: Int): Iterable[P] = {
    assert(howMany > 0, "Sample size must be greater than 0")

    if (isExhausted)
      throw new NoSuchElementException("Grid space has been exhausted of all values.")

    Seq.fill(scala.math.min(howMany, (max - i).toInt))(sample)
  }

  def isExhausted: Boolean = i >= max
}

case class GridRow(label: String, values: Seq[_])
case class GridProperty(factor: Long, length: Int)
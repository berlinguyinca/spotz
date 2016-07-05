package com.eharmony.spotz.optimizer.grid

import com.eharmony.spotz.space.{Point, PointBuilder, Space}

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
class GridSpace(gridParams: Iterable[(String, Iterable[_])]) extends Space[Point] {
  assert(gridParams.nonEmpty, "No grid parameters have been specified")

  // Expand the entire grid space.  The memory required is linear in the sum of lengths of all the iterables
  val gridSpace = gridParams.map { case (label, it) => (label, it.toSeq) } toSeq

  // pre-compute the length of each grid iterable, indexed into a Seq
  val gridLengths = gridSpace.map { case (label, seq) => seq.length }

  // pre-compute the divisible factor and store along with the length inside GridProperty
  // http://phrogz.net/lazy-cartesian-product
  val gridProperties = gridLengths.foldRight(ArrayBuffer[GridProperty]()) { case (l: Int, b) =>
    if (b.isEmpty) GridProperty(1, l) +=: b
    else GridProperty(b.head.length * b.head.factor, l) +=: b
  }

  val max = gridLengths.product
  var i = -1

  override def sample: Point = {
    i += 1

    if (i >= max)
      throw new RuntimeException("Grid space has been exhausted of all values.")

    val gridIndices = gridProperties.map { case GridProperty(factor, length) => (i / factor) % length }

    gridIndices.zipWithIndex.foldLeft(new PointBuilder) { case (pb, (arrayIndex, gridIndex)) =>
      pb.withHyperParameter(gridSpace(gridIndex)._1, gridSpace(gridIndex)._2(arrayIndex))
    }.build
  }

  override def seed(seed: Long): Space[Point] = ???
}

case class GridProperty(factor: Int, length: Int)
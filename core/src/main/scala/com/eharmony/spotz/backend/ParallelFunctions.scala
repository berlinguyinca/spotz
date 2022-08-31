package com.eharmony.spotz.backend

import com.eharmony.spotz.objective.Objective
import com.eharmony.spotz.optimizer.grid.Grid
import com.eharmony.spotz.optimizer.hyperparam.RandomSampler

import scala.collection.parallel.CollectionConverters.ImmutableIterableIsParallelizable
import scala.reflect.ClassTag

/**
  * Use scala parallel collections to parallelize the optimizer computation.
  * Parallel collections are backed by threads, so sampling occurs in its
  * respective thread.
  */
trait ParallelFunctions extends BackendFunctions {

  /**
    * Each trial point will be generated from a rng seeded by (trial number + seed).
    * This ensures the any randomness is repeatable for future trial runs of the
    * same seed.  The points are then applied in the objective function in parallel
    * and a single (point, loss) tuple is returned from the objective.  A reducer
    * then returns the best point and loss.
    *
    * @param startIndex the starting trial index
    * @param batchSize the number of points to sample beginning at the startIndex
    * @param objective the function on which a point is applied
    * @param reducer the reducer function to use on all the various points and losses
    *                generated
    * @param hyperParams defined hyper parameters from which to sample
    * @param sampleFunction sample a point from the defined hyper parameter space
    * @param seed the seed
    * @tparam P point type
    * @tparam L loss type
    * @return the best point with the best loss as a tuple
    */
  protected override def bestRandomPointAndLoss[P, L](
    startIndex: Long,
    batchSize: Long,
    objective: Objective[P, L],
    reducer: ((P, L), (P, L)) => (P, L),
    hyperParams: Map[String, RandomSampler[_]],
    sampleFunction: (Map[String, RandomSampler[_]], Long) => P,
    seed: Long = 0): (P, L) = {

    val pointsAndLosses = (startIndex until (startIndex + batchSize)).par.map { trial =>
      val point = sampleFunction(hyperParams, seed + trial)
      (point, objective(point))
    }
    pointsAndLosses.reduce(reducer)
  }

  /**
    * Each trial point will be obtained by doing an index lookup inside a
    * <code>Grid</code> object, similarly to how a lookup is done with an
    * <code>IndexedSeq</code> using the apply method.
    *
    * @param startIndex the starting trial index
    * @param batchSize the number of points to sample beginning at the startIndex
    * @param objective the function on which a point is applied
    * @param grid Grid object from which to obtain trial points
    * @param reducer reduce function to reduce a (point, loss) tuple
    * @param p ClassTag for type P
    * @param l ClassTag for type L
    * @tparam P point type
    * @tparam L loss type
    * @return the best point with the best loss as a tuple
    */
  protected override def bestGridPointAndLoss[P, L](
      startIndex: Long,
      batchSize: Long,
      objective: Objective[P, L],
      grid: Grid[P],
      reducer: ((P, L), (P, L)) => (P, L))
      (implicit p: ClassTag[P], l: ClassTag[L]): (P, L) = {

    val pointsAndLosses = (startIndex until (startIndex + batchSize)).par.map { trial =>
      val point = grid(trial)
      (point, objective(point))
    }
    pointsAndLosses.reduce(reducer)
  }
}

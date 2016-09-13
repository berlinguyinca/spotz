package com.eharmony.spotz.backend

import com.eharmony.spotz.objective.Objective
import com.eharmony.spotz.optimizer.grid.Grid
import com.eharmony.spotz.optimizer.hyperparam.RandomSampler

import scala.reflect.ClassTag

/**
  * This trait contains the functions that are executed by the distributed
  * computation framework.  Currently Spark and parallel collections are
  * supported.  All optimizers will delegate to these functions to parallelize
  * the optimization computation.
  *
  * The methods provided here will sample batchSize trial points starting with
  * the given trial start index.  The points are then applied in the objective
  * function in parallel and a single (point, loss) tuple is returned from the
  * objective.  A reducer then returns the best point and loss.
  *
  * @author vsuthichai
  */
trait BackendFunctions {
  protected def bestRandomPointAndLoss[P, L](
    startIndex: Long,
    batchSize: Long,
    objective: Objective[P, L],
    reducer: ((P, L), (P, L)) => (P, L),
    hyperParams: Map[String, RandomSampler[_]],
    sampleFunction: (Map[String, RandomSampler[_]], Long) => P,
    seed: Long = 0): (P, L)

  protected def bestGridPointAndLoss[P, L](
    startIndex: Long,
    batchSize: Long,
    objective: Objective[P, L],
    space: Grid[P],
    reducer: ((P, L), (P, L)) => (P, L))
    (implicit c: ClassTag[P], p: ClassTag[L]): (P, L)
}

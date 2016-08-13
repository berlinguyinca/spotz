package com.eharmony.spotz.backend

import com.eharmony.spotz.objective.Objective
import com.eharmony.spotz.optimizer.RandomSampler
import com.eharmony.spotz.optimizer.grid.GridSpace

import scala.reflect.ClassTag

/**
  * Use scala parallel collections to parallelize the optimizer computation.
  */
trait ParallelFunctions extends BackendFunctions {
  protected override def bestRandomPointAndLoss[P, L](startIndex: Long,
                                                      batchSize: Long,
                                                      objective: Objective[P, L],
                                                      reducer: ((P, L), (P, L)) => (P, L),
                                                      hyperParams: Map[String, RandomSampler[_]],
                                                      seed: Long = 0,
                                                      sampleFunction: (Map[String, RandomSampler[_]], Long) => P): (P, L) = {
    val pointsAndLosses = (startIndex until (startIndex + batchSize)).par.map { trial =>
      val point = sampleFunction(hyperParams, seed + trial)
      (point, objective(point))
    }
    pointsAndLosses.reduce(reducer)
  }

  protected override def bestGridPointAndLoss[P, L](startIndex: Long,
                                                    batchSize: Long,
                                                    objective: Objective[P, L],
                                                    space: GridSpace[P],
                                                    reducer: ((P, L), (P, L)) => (P, L))
                                                    (implicit c: ClassTag[P], p: ClassTag[L]): (P, L) = {
    val pointsAndLosses = (startIndex until (startIndex + batchSize)).par.map { trial =>
      val point = space(trial)
      (point, objective(point))
    }
    pointsAndLosses.reduce(reducer)
  }
}

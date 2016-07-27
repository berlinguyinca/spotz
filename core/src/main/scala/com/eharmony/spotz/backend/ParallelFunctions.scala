package com.eharmony.spotz.backend

import com.eharmony.spotz.objective.Objective
import com.eharmony.spotz.optimizer.random.RandomSpace

import scala.reflect.ClassTag

/**
  * @author vsuthichai
  */
trait ParallelFunctions extends BackendFunctions {
  override def bestRandomPoint[P, L](startIndex: Long,
                                     batchSize: Long,
                                     objective: Objective[P, L],
                                     space: RandomSpace[P],
                                     reducer: ((P, L), (P, L)) => (P, L)): (P, L) = {
    val pointsAndLosses = (startIndex to (startIndex + batchSize)).par.map { trial =>
      val rngModifiedSpace = space.setSeed(space.seed + trial)
      val point = rngModifiedSpace.sample
      (point, objective(point))
    }

    pointsAndLosses.reduce(reducer)
  }

  override def bestPointAndLoss[P, L](gridPoints: Seq[P],
                                      objective: Objective[P, L],
                                      reducer: ((P, L), (P, L)) => (P, L))
                                      (implicit c: ClassTag[P], p: ClassTag[L]): (P, L) = {
    gridPoints.par.map(point => (point, objective(point))).reduce(reducer)
  }
}

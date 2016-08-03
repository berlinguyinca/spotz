package com.eharmony.spotz.backend

import com.eharmony.spotz.objective.Objective
import com.eharmony.spotz.optimizer.grid.GridSpace
import com.eharmony.spotz.optimizer.random.RandomSpace

import scala.reflect.ClassTag

/**
  * @author vsuthichai
  */
trait ParallelFunctions extends BackendFunctions {
  protected override def bestRandomPoint[P, L](startIndex: Long,
                                               batchSize: Long,
                                               objective: Objective[P, L],
                                               space: RandomSpace[P],
                                               reducer: ((P, L), (P, L)) => (P, L)): (P, L) = {
    val pointsAndLosses = (startIndex until (startIndex + batchSize)).par.map { trial =>
      val rngModifiedSpace = space.setSeed(space.seed + trial)
      val point = rngModifiedSpace.sample
      (point, objective(point))
    }

    pointsAndLosses.reduce(reducer)
  }

  protected override def bestPointAndLoss[P, L](startIndex: Long,
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

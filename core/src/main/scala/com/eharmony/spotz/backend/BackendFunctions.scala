package com.eharmony.spotz.backend

import com.eharmony.spotz.objective.Objective
import com.eharmony.spotz.optimizer.grid.GridSpace
import com.eharmony.spotz.optimizer.random.RandomSpace

import scala.reflect.ClassTag

/**
  * @author vsuthichai
  */
trait BackendFunctions {
  protected def bestRandomPoint[P, L](startIndex: Long,
                                      batchSize: Long,
                                      objective: Objective[P, L],
                                      space: RandomSpace[P],
                                      reducer: ((P, L), (P, L)) => (P, L)): (P, L)

  protected def bestPointAndLoss[P, L](startIndex: Long,
                                       batchSize: Long,
                                       objective: Objective[P, L],
                                       space: GridSpace[P],
                                       reducer: ((P, L), (P, L)) => (P, L))
                                      (implicit c: ClassTag[P], p: ClassTag[L]): (P, L)
}

trait BackendType
case object SPARK_BACKEND extends BackendType
case object PAR_BACKEND extends BackendType
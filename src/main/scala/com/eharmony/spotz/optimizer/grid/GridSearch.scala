package com.eharmony.spotz.optimizer.grid

import com.eharmony.spotz.backend.spark.SparkFunctions
import com.eharmony.spotz.objective.Objective
import com.eharmony.spotz.optimizer.random.{RandomSearchResult, RandomSpace}
import com.eharmony.spotz.optimizer.{SparkBaseOptimizer, StopStrategy, _}
import org.apache.spark.{Logging, SparkContext}
import org.joda.time.{DateTime, Duration}

import scala.annotation.tailrec
import scala.math.Ordering
import scala.reflect.ClassTag

/**
  * @author vsuthichai
  */
class GridSearch[P, L](
    @transient val sc: SparkContext,
    trialBatchSize: Int = 100)
    (implicit val ord: Ordering[(P, L)])
  extends SparkBaseOptimizer[P, L, GridSpace[P], GridSearchResult[P, L]]
    with SparkFunctions {

  override def optimize(objective: Objective[P, L],
                        space: GridSpace[P],
                        reducer: Reducer[(P, L)])
                       (implicit c: ClassTag[P], p: ClassTag[L]): GridSearchResult[P, L] = {

    val startTime = DateTime.now()
    val firstPoint = space.sample
    val firstLoss = objective(firstPoint)

    // Last three arguments maintain the best point and loss and the trial count
    gridSearch(objective, space, reducer, startTime, firstPoint, firstLoss, 1)
  }

  @tailrec
  private[this] def gridSearch(objective: Objective[P, L], space: GridSpace[P], reducer: Reducer[(P, L)],
                               startTime: DateTime, bestPointSoFar: P, bestLossSoFar: L, trialsSoFar: Long)
                              (implicit c: ClassTag[P], p: ClassTag[L]): GridSearchResult[P, L] = {

    val endTime = DateTime.now()
    val elapsedTime = new Duration(startTime, endTime)

    space.isExhausted match {
      case true =>
        GridSearchResult(bestPointSoFar, bestLossSoFar, startTime, endTime, trialsSoFar, elapsedTime)
      case false =>
        val points = space.sample(trialBatchSize)
        val (bestPoint, bestLoss) = reducer((bestPointSoFar, bestLossSoFar), bestPointAndLoss(points.toSeq, objective, reducer))
        gridSearch(objective, space, reducer, startTime, bestPoint, bestLoss, trialsSoFar)
    }
  }
}

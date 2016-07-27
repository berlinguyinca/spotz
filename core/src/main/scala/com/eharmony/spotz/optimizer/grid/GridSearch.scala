package com.eharmony.spotz.optimizer.grid

import com.eharmony.spotz.backend.{BackendFunctions, ParallelFunctions, SparkFunctions}
import com.eharmony.spotz.objective.Objective
import com.eharmony.spotz.optimizer.AbstractOptimizer
import org.apache.spark.SparkContext
import org.joda.time.{DateTime, Duration}

import scala.annotation.tailrec
import scala.math.Ordering
import scala.reflect.ClassTag

/**
  * @author vsuthichai
  */
abstract class GridSearch[P, L]
    (trialBatchSize: Int = 100)
    (implicit val ord: Ordering[(P, L)])
  extends AbstractOptimizer[P, L, GridSpace[P], GridSearchResult[P, L]]
    with BackendFunctions {

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

class ParGridSearch[P, L](trialBatchSize: Int = 100)(implicit ord: Ordering[(P, L)])
  extends GridSearch[P, L](trialBatchSize)(ord) with ParallelFunctions

class SparkGridSearch[P, L](@transient val sc: SparkContext, trialBatchSize: Int = 100)(implicit ord: Ordering[(P, L)])
  extends GridSearch[P, L](trialBatchSize)(ord) with SparkFunctions
package com.eharmony.spotz.optimizer.grid

import com.eharmony.spotz.backend.{BackendFunctions, ParallelFunctions, SparkFunctions}
import com.eharmony.spotz.objective.Objective
import com.eharmony.spotz.optimizer.AbstractOptimizer
import com.eharmony.spotz.util.{DurationUtils, Logging}
import org.apache.spark.SparkContext
import org.joda.time.{DateTime, Duration}

import scala.annotation.tailrec
import scala.math.Ordering
import scala.reflect.ClassTag

/**
  * Grid search implementation.
  *
  * This class accepts a hyper parameter space on which to search using a grid search algorithm.
  * The parameter space must be specified through a Map where the key is a String that
  * identifies the hyper parameter label and the value is an Iterable type.  Grid search will iteratively
  * and exhaustively search over all possible combinations of values specified by the Iterable's in the Map.
  *
  * The implementation specifies two type parameters, P, the point representation of hyper parameters
  * and L, the resulting loss from evaluating the objective function.  The best point is kept track of while
  * iterating over the grid values.  Once the grid values have been exhausted, the search algorithm ends.
  *
  * Internally, points are evaluted in batches to allow intermediate updates from whatever distributed
  * computation framework is being used.
  *
  * @author vsuthichai
  */
abstract class GridSearch[P, L]
    (trialBatchSize: Int)
    (implicit ord: Ordering[(P, L)], factory: Map[String, _] => P)
  extends AbstractOptimizer[P, L, Map[String, Iterable[Any]], GridSearchResult[P, L]]
  with BackendFunctions
  with Logging {

  override protected def optimize(objective: Objective[P, L],
                                  paramSpace: Map[String, Iterable[Any]],
                                  reducer: Reducer[(P, L)])
                                 (implicit c: ClassTag[P], p: ClassTag[L]): GridSearchResult[P, L] = {
    val space = new Grid[P](paramSpace)
    val startTime = DateTime.now()
    val firstPoint = space(0)
    val firstLoss = objective(firstPoint)

    // Last three arguments maintain the best point and loss and the trial count
    gridSearch(objective, space, reducer, startTime, firstPoint, firstLoss, 1)
  }

  @tailrec
  private def gridSearch(objective: Objective[P, L], space: Grid[P], reducer: Reducer[(P, L)],
                         startTime: DateTime, bestPointSoFar: P, bestLossSoFar: L, trialsSoFar: Long)
                        (implicit c: ClassTag[P], p: ClassTag[L]): GridSearchResult[P, L] = {
    val endTime = DateTime.now()
    val elapsedTime = new Duration(startTime, endTime)

    info(s"Best point and loss after $trialsSoFar trials and ${DurationUtils.format(elapsedTime)} : $bestPointSoFar loss: $bestLossSoFar")

    trialsSoFar >= space.size match {
      case true =>
        GridSearchResult(bestPointSoFar, bestLossSoFar, startTime, endTime, trialsSoFar, elapsedTime)
      case false =>
        val batchSize = scala.math.min(space.size - trialsSoFar, trialBatchSize)
        val (bestPoint, bestLoss) = reducer((bestPointSoFar, bestLossSoFar), bestGridPointAndLoss(trialsSoFar, batchSize, objective, space, reducer))
        gridSearch(objective, space, reducer, startTime, bestPoint, bestLoss, trialsSoFar + batchSize)
    }
  }
}

/**
  * Grid search backed by parallel collections.
  *
  * @param trialBatchSize default batch size specifying number of points to batch process
  * @param ord Ordering for point type P
  * @param factory a function that takes a Map of sampled hyper parameter values and instanties the point P
  * @tparam P point type representation
  * @tparam L loss type representation
  */
class ParGridSearch[P, L](
    trialBatchSize: Int = 1000000)
    (implicit val ord: Ordering[(P, L)], factory: Map[String, _] => P)
  extends GridSearch[P, L](trialBatchSize)(ord, factory)
  with ParallelFunctions

/**
  * Grid search backed by Spark.
  *
  * @param sc SparkContext
  * @param trialBatchSize default batch size specifying number of points to batch process
  * @param ord Ordering for point type P
  * @param factory a function that takes a Map of sampled hyper parameter values and instanties the point P
  * @tparam P point type representation
  * @tparam L loss type representation
  */
class SparkGridSearch[P, L](
    @transient val sc: SparkContext,
    trialBatchSize: Int = 1000000)
    (implicit val ord: Ordering[(P, L)], factory: Map[String, _] => P)
  extends GridSearch[P, L](trialBatchSize)(ord, factory)
  with SparkFunctions
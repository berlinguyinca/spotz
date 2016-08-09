package com.eharmony.spotz.optimizer.random

import com.eharmony.spotz.backend.{BackendFunctions, ParallelFunctions, SparkFunctions}
import com.eharmony.spotz.objective.Objective
import com.eharmony.spotz.optimizer._
import com.eharmony.spotz.util.{DurationUtils, Logging}
import org.apache.spark.SparkContext
import org.joda.time.{DateTime, Duration}

import scala.annotation.tailrec
import scala.math.Ordering
import scala.reflect.ClassTag

/**
  * Random search implementation.
  *
  * This search algorithm samples from a predefined Map of RandomSamplers to materialize hyper parameter
  * values.  This hyper parameter values are represented through a type parameter P, the point representation.
  * These points are then evaluated with the supplied objective function.  A StopStrategy object must be
  * supplied by the caller to specify search algorithm stopping criteria.
  *
  * @author vsuthichai
  */
abstract class RandomSearch[P, L](
    stopStrategy: StopStrategy,
    trialBatchSize: Int,
    seed: Int = 0)
    (implicit val ord: Ordering[(P, L)], factory: Map[String, _] => P)
  extends AbstractOptimizer[P, L, Map[String, RandomSampler[_]], RandomSearchResult[P, L]]
  with BackendFunctions
  with Logging {

  override def optimize(objective: Objective[P, L],
                        paramSpace: Map[String, RandomSampler[_]],
                        reducer: Reducer[(P, L)])
                       (implicit c: ClassTag[P], p: ClassTag[L]): RandomSearchResult[P, L] = {
    val space = new RandomSpace[P](paramSpace, seed)
    val startTime = DateTime.now()
    val firstPoint = space.sample
    val firstLoss = objective(firstPoint)

    // Last three arguments maintain the best point and loss and the trial count
    randomSearch(objective = objective, space = space, reducer = reducer, startTime = startTime,
      bestPointSoFar = firstPoint, bestLossSoFar = firstLoss, trialsSoFar = 1)
  }

  /**
    * A tail recursive method that performs the random search.  We keep track of the best point and the best loss
    * represented by P and L.  This function delegates to the BackendFunctions trait to parallelize the objective
    * function evaluations.  The method completes after a caller specified number of trials and/or elapsed time
    * duration.
    *
    * @param objective the objective function
    * @param space representation of hyper parameter space from which to sample hyper parameter points
    * @param reducer the function used to reduce points
    * @param startTime the time at which this optimization job started
    * @param bestPointSoFar the best point discovered so far
    * @param bestLossSoFar the best loss discovered so far
    * @param trialsSoFar the number of objective function evaluations performed so far
    * @param c ClassTag for P
    * @param p ClassTag for C
    * @return a <code>RandomSearchResult</code>
    */
  @tailrec
  private[this] def randomSearch(objective: Objective[P, L],
                                 space: RandomSpace[P],
                                 reducer: Reducer[(P, L)],
                                 startTime: DateTime,
                                 bestPointSoFar: P,
                                 bestLossSoFar: L,
                                 trialsSoFar: Long)
                                (implicit c: ClassTag[P], p: ClassTag[L]): RandomSearchResult[P, L] = {
    val endTime = DateTime.now()
    val elapsedTime = new Duration(startTime, endTime)

    info(s"Best point and loss after $trialsSoFar trials and ${DurationUtils.format(elapsedTime)} : $bestPointSoFar loss: $bestLossSoFar")

    stopStrategy.shouldStop(trialsSoFar, elapsedTime) match {
      case true  =>
        // Base case, End recursion
        new RandomSearchResult[P, L](bestPointSoFar, bestLossSoFar, startTime, endTime, trialsSoFar, elapsedTime)

      case false =>
        val batchSize = scala.math.min(stopStrategy.getMaxTrials - trialsSoFar, trialBatchSize)
        // TODO: Adaptive batch sizing
        //val batchSize = nextBatchSize(None, elapsedTime, currentBatchSize, trialsSoFar, null, stopStrategy.getMaxTrials)

        val (bestPoint, bestLoss) = reducer((bestPointSoFar, bestLossSoFar),
          bestRandomPoint(trialsSoFar, batchSize, objective, space, reducer))

        // Last 3 args maintain the state
        randomSearch(objective, space, reducer, startTime, bestPoint, bestLoss, trialsSoFar + batchSize)
    }
  }
}

/**
  * This implementation uses parallel collections to evaluate the objective function.  Internally,
  * threads are used.
  *
  * @param stopStrategy the stop strategy criteria specifying when the search should end
  * @param trialBatchSize batch size specifying the number of points to process per epoch
  * @param ord an implicit Ordering for the point representation type parameter P
  * @param factory an implicit function specifying how to instantiate P given a Map of sampled hyper parameter values
  * @tparam P point type representation
  * @tparam L loss type representation
  */
class ParRandomSearch[P, L](
    stopStrategy: StopStrategy,
    trialBatchSize: Int = 1000000)
    (implicit ord: Ordering[(P, L)], factory: Map[String, _] => P)
  extends RandomSearch[P, L](stopStrategy, trialBatchSize)(ord, factory)
  with ParallelFunctions

/**
  * Random search on top of Spark.
  *
  * @param stopStrategy the stop strategy criteria specifying when the search should end
  * @param trialBatchSize batch size specifying the number of points to process per epoch
  * @param ord an implicit Ordering for the point representation type parameter P
  * @param factory an implicit function specifying how to instantiate P given a Map of sampled hyper parameter values
  * @tparam P point type representation
  * @tparam L loss type representation
  */
class SparkRandomSearch[P, L](
    @transient val sc: SparkContext,
    stopStrategy: StopStrategy,
    trialBatchSize: Int = 1000000)
    (implicit ord: Ordering[(P, L)], factory: Map[String, _] => P)
  extends RandomSearch[P, L](stopStrategy, trialBatchSize)(ord, factory)
  with SparkFunctions

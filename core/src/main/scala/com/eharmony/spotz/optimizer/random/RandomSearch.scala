package com.eharmony.spotz.optimizer.random

import com.eharmony.spotz.backend.{BackendFunctions, ParallelFunctions, SparkFunctions}
import com.eharmony.spotz.objective.Objective
import com.eharmony.spotz.optimizer._
import com.eharmony.spotz.util.{DurationUtils, Logger}
import org.apache.spark.SparkContext
import org.joda.time.{DateTime, Duration}

import scala.annotation.tailrec
import scala.math.Ordering
import scala.reflect.ClassTag

/**
  * @author vsuthichai
  */
abstract class RandomSearch[P, L]
    (paramSpace: Map[String, RandomSampler[_]], stopStrategy: StopStrategy, trialBatchSize: Int, seed: Int = 0)
    (implicit val ord: Ordering[(P, L)], factory: Map[String, _] => P)
  extends AbstractOptimizer[P, L,  RandomSearchResult[P, L]]
    with BackendFunctions {

  val LOG = Logger[this.type]()

  override def optimize(objective: Objective[P, L],
                        reducer: Reducer[(P, L)])
                       (implicit c: ClassTag[P], p: ClassTag[L]): RandomSearchResult[P, L] = {
    val space = new RandomSpace[P](paramSpace)
    val startTime = DateTime.now()
    val firstPoint = space.sample
    val firstLoss = objective(firstPoint)

    // Last three arguments maintain the best point and loss and the trial count
    randomSearch(objective, space, reducer, startTime, firstPoint, firstLoss, 1)
  }

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

    LOG.info(s"Best point and loss after $trialsSoFar trials and ${DurationUtils.format(elapsedTime)} : $bestPointSoFar loss: $bestLossSoFar")

    stopStrategy.shouldStop(trialsSoFar, elapsedTime) match {
      case true  =>
        // Base case, End recursion
        new RandomSearchResult[P, L](bestPointSoFar, bestLossSoFar, startTime, endTime, trialsSoFar, elapsedTime)

      case false =>
        val batchSize = scala.math.min(stopStrategy.getMaxTrials - trialsSoFar, trialBatchSize).toInt
        val (bestPoint, bestLoss) = reducer((bestPointSoFar, bestLossSoFar),
          bestRandomPoint(trialsSoFar, batchSize, objective, space, reducer))

        // Last 3 args maintain the state
        randomSearch(objective, space, reducer, startTime, bestPoint, bestLoss, trialsSoFar + batchSize)
    }
  }
}

class ParRandomSearch[P, L]
    (paramSpace: Map[String, RandomSampler[_]], stopStrategy: StopStrategy, trialBatchSize: Int = 1000000)
    (implicit ord: Ordering[(P, L)], factory: Map[String, _] => P)
  extends RandomSearch[P, L](paramSpace, stopStrategy, trialBatchSize)(ord, factory) with ParallelFunctions

class SparkRandomSearch[P, L]
    (@transient val sc: SparkContext, paramSpace: Map[String, RandomSampler[_]], stopStrategy: StopStrategy, trialBatchSize: Int = 1000000)
    (implicit ord: Ordering[(P, L)], factory: Map[String, _] => P)
  extends RandomSearch[P, L](paramSpace, stopStrategy, trialBatchSize)(ord, factory)
    with SparkFunctions

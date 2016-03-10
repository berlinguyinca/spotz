package com.eharmony.spotz.optimizer

import com.eharmony.spotz.optimizer.framework.Framework
import com.eharmony.spotz.optimizer.stop.StopStrategy
import com.eharmony.spotz.objective.Objective
import com.eharmony.spotz.space.Space
import org.joda.time.format.PeriodFormatterBuilder
import org.joda.time.{DateTime, Duration}

import scala.annotation.tailrec
import scala.math.Ordering

/**
 * @author vsuthichai
 */
class RandomSearch[P, L](
    framework: Framework[P, L],
    stopStrategy: StopStrategy,
    trialBatchSize: Int = 100000)
    (implicit pointLossOrdering: Ordering[(P, L)])
  extends BaseOptimizer[P, L] {

  override def optimize(objective: Objective[P, L],
                        space: Space[P],
                        reducer: Reducer[(P, L)]): RandomSearchResult[P, L] = {
    val startTime = DateTime.now()
    val rngModifiedSpace = space.seed(0)
    val firstPoint = rngModifiedSpace.sample
    val firstLoss = objective(firstPoint)

    // Last three arguments maintain the best point and loss and the trial count
    randomSearch(objective, space, reducer, startTime, firstPoint, firstLoss, 1)
  }

  @tailrec
  private[this] def randomSearch(objective: Objective[P, L],
                                 space: Space[P],
                                 reducer: Reducer[(P, L)],
                                 startTime: DateTime,
                                 bestPointSoFar: P,
                                 bestLossSoFar: L,
                                 trialsSoFar: Long): RandomSearchResult[P, L] = {

    val endTime = DateTime.now()
    val elapsedTime = new Duration(startTime, endTime)

    stopStrategy.shouldStop(trialsSoFar, elapsedTime) match {
      case true  =>
        // Base case, End recursion
        new RandomSearchResult[P, L](bestPointSoFar, bestLossSoFar, startTime, endTime, trialsSoFar, elapsedTime)

      case false =>
        val batchSize = scala.math.min(stopStrategy.getMaxTrials - trialsSoFar, trialBatchSize).toInt
        val (bestPoint, bestLoss) = reducer((bestPointSoFar, bestLossSoFar),
                                            framework.bestRandomPoint(trialsSoFar, batchSize, objective, space, reducer))
        // Last 3 args maintain the state
        randomSearch(objective, space, reducer, startTime, bestPoint, bestLoss, trialsSoFar + batchSize)
    }
  }
}

class RandomSearchResult[P, L](
    bestPoint: P,
    bestLoss: L,
    startTime: DateTime,
    endTime: DateTime,
    totalTrials: Long,
    elapsedTime: Duration)
  extends OptimizerResult[P, L](bestPoint, bestLoss) {

  override def toString = {
    val formatter = new PeriodFormatterBuilder()
      .appendDays().appendSuffix("d")
      .appendHours().appendSuffix("h")
      .appendMinutes().appendSuffix("m")
      .appendSeconds().appendSuffix("s")
      .appendMillis().appendSuffix("ms")
      .toFormatter

      s"RandomSearchResult(bestPoint=$bestPoint, bestLoss=$bestLoss, " +
      s"totalTrials=$totalTrials, duration=${formatter.print(elapsedTime.toPeriod)}"
  }
}

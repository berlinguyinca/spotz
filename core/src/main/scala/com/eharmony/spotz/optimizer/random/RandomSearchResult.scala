package com.eharmony.spotz.optimizer.random

import com.eharmony.spotz.optimizer.OptimizerResult
import com.eharmony.spotz.util.DurationUtils
import org.joda.time.{DateTime, Duration}

/**
  * @author vsuthichai
  */
case class RandomSearchResult[P, L](
    bestPoint: P,
    bestLoss: L,
    startTime: DateTime,
    endTime: DateTime,
    totalTrials: Long,
    elapsedTime: Duration)
  extends OptimizerResult[P, L](bestPoint, bestLoss) {

  override def toString = {
    s"RandomSearchResult(bestPoint=$bestPoint, bestLoss=$bestLoss, " +
      s"totalTrials=$totalTrials, duration=${DurationUtils.format(elapsedTime)}"
  }
}

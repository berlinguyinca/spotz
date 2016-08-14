package com.eharmony.spotz.optimizer

import org.joda.time.Duration

/**
  * @author vsuthichai
  */
sealed trait StopStrategy extends Serializable {
  final val UNLIMITED = Long.MaxValue
  final val FOREVER = new Duration(Long.MaxValue)

  def getMaxTrials: Long = UNLIMITED
  def getMaxDuration: Duration = FOREVER

  def shouldStop[P, L](optimizerState: OptimizerState[P, L]): Boolean
}

/**
  * Stop after a maximum number of executed trials.
  *
  * @param maxTrials
  */
class MaxTrialsStop(maxTrials: Long) extends StopStrategy {
  assert(maxTrials > 0, "Must specify greater than 0 trials.")
  override def getMaxTrials: Long = maxTrials
  override def shouldStop[P, L](optimizerState: OptimizerState[P, L]): Boolean = {
    optimizerState.trialsSoFar >= maxTrials
  }
}

/**
  * Stop after an elapsed time duration.
  *
  * @param maxDuration
  */
class TimedStop(maxDuration: Duration) extends StopStrategy {
  assert(maxDuration.toStandardSeconds.getSeconds > 0, "Must specify a longer duration")

  override def getMaxDuration: Duration = maxDuration
  override def shouldStop[P, L](optimizerState: OptimizerState[P, L]): Boolean = {
    optimizerState.elapsedTime.getMillis >= maxDuration.getMillis
  }
}

/**
  * Stop after an elapsed time duration or after a maximum number of executed trials.
  *
  * @param maxTrials
  * @param maxDuration
  */
class MaxTrialsOrMaxDurationStop(maxTrials: Long, maxDuration: Duration) extends StopStrategy {
  override def getMaxTrials: Long = maxTrials
  override def getMaxDuration: Duration = maxDuration
  override def shouldStop[P, L](optimizerState: OptimizerState[P, L]): Boolean = {
    optimizerState.trialsSoFar >= maxTrials || optimizerState.elapsedTime.getMillis >= maxDuration.getMillis
  }
}

/**
  * Stop after an optimizer has finished running.  This should never be used for RandomSearch because
  * it will never complete without some specific stopping criteria.
  */
class OptimizerFinishes extends StopStrategy {
  override def shouldStop[P, L](optimizerState: OptimizerState[P, L]): Boolean = {
    optimizerState.optimizerFinished
  }
}


class StopStrategyPredicate(f: OptimizerState[_, _] => Boolean) extends StopStrategy {
  override def shouldStop[P, L](optimizerState: OptimizerState[P, L]): Boolean = f(optimizerState)
}

/**
  * Companion factory object to instantiate various stop strategies.
  */
object StopStrategy {
  def stopAfterMaxDuration(maxDuration: Duration): StopStrategy = new TimedStop(maxDuration)
  def stopAfterMaxTrials(maxTrials: Long): StopStrategy = new MaxTrialsStop(maxTrials)
  def stopAfterMaxTrialsOrMaxDuration(maxTrials: Long, maxDuration: Duration): StopStrategy = {
    new MaxTrialsOrMaxDurationStop(maxTrials, maxDuration)
  }
  def stopWhenOptimizerFinishes: StopStrategy = new OptimizerFinishes
  def stopWhenPredicateIsTrue(f: OptimizerState[_, _] => Boolean): StopStrategy = new StopStrategyPredicate(f)
}

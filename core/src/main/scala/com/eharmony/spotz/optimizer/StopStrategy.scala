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
  def shouldStop(trialsSoFar: Long, timeSinceFirstTrial: Duration): Boolean
}

// TODO
/**
  * A context object describing the current state of an optimizer.  It keeps track of state
  * such as the best point, the best loss, elapsed time, trials executed so far, and other
  * important information that could be used to specify some stopping criteria for the
  * optimizer.
  *
  * @param foo
  * @tparam P
  * @tparam L
  */
case class StopContext[P, L](foo: Any)

/**
  * Stop after a maximum number of executed trials.
  *
  * @param maxTrials
  */
class MaxTrialsStop(maxTrials: Long) extends StopStrategy {
  assert(maxTrials > 0, "Must specify greater than 0 trials.")
  override def getMaxTrials: Long = maxTrials
  override def shouldStop(trialsSoFar: Long, durationSinceFirstTrial: Duration): Boolean = {
    trialsSoFar >= maxTrials
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
  override def shouldStop(trialsSoFar: Long, durationSinceFirstTrial: Duration): Boolean = {
    durationSinceFirstTrial.getMillis >= maxDuration.getMillis
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
  override def shouldStop(trialsSoFar: Long, durationSinceFirstTrial: Duration): Boolean = {
    trialsSoFar >= maxTrials || durationSinceFirstTrial.getMillis >= maxDuration.getMillis
  }
}

object OptimizerFinishes extends StopStrategy {
  override def shouldStop(trialsSoFar: Long, durationSinceFirstTrial: Duration): Boolean = false
}

// TODO
/**
  * Stop after some criteria defined by the user.
  *
  * @param f
  * @tparam P
  * @tparam L
  */
class StopStrategyPredicate[P, L](f: (StopContext[P, L]) => Boolean) {
  def shouldStop(stopContext: StopContext[P, L]) = f(stopContext)
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
  def stopWhenOptimizerFinishes: StopStrategy = OptimizerFinishes
}

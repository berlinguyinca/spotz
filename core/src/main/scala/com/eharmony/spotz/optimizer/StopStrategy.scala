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

// TODO: Finish this.
case class StopContext[P, L](foo: Any)

class MaxTrialsStop(maxTrials: Long) extends StopStrategy {
  assert(maxTrials > 0, "Must specify greater than 0 trials.")
  override def getMaxTrials: Long = maxTrials
  override def shouldStop(trialsSoFar: Long, durationSinceFirstTrial: Duration): Boolean = {
    trialsSoFar >= maxTrials
  }
}

class TimedStop(maxDuration: Duration) extends StopStrategy {
  assert(maxDuration.toStandardSeconds.getSeconds > 0, "Must specify a longer duration")
  override def getMaxDuration: Duration = maxDuration
  override def shouldStop(trialsSoFar: Long, durationSinceFirstTrial: Duration): Boolean = {
    durationSinceFirstTrial.getMillis >= maxDuration.getMillis
  }
}

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

// TODO: Finish this.
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

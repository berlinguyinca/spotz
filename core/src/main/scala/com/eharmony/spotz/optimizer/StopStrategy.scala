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

/**
 * Companion factory object to instantiate various stop strategies.
 */
object StopStrategy {
  def stopAfterMaxDuration(maxDuration: Duration): TimedStop = new TimedStop(maxDuration)
  def stopAfterMaxTrials(maxTrials: Long): MaxTrialsStop = new MaxTrialsStop(maxTrials)
  def stopAfterMaxTrialsOrMaxDuration(maxTrials: Long, maxDuration: Duration): MaxTrialsOrMaxDurationStop = {
    new MaxTrialsOrMaxDurationStop(maxTrials, maxDuration)
  }
}

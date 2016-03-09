package com.eharmony.spotz.optimizer.stop

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

  override def getMaxDuration: Duration = maxDuration
  override def shouldStop(trialsSoFar: Long, durationSinceFirstTrial: Duration): Boolean = {
    durationSinceFirstTrial.getMillis >= maxDuration.getMillis
  }
}

object StopStrategy {
  def stopAfterMaxDuration(maxDuration: Duration): TimedStop = new TimedStop(maxDuration)

  def stopAfterMaxTrials(maxTrials: Long): MaxTrialsStop = new MaxTrialsStop(maxTrials)

  def stopAfterMaxTrialsOrMaxDuration(maxTrials: Long, maxDuration: Duration) = new StopStrategy {
    val maxTrialsStop = new MaxTrialsStop(maxTrials)
    val timedStop = new TimedStop(maxDuration)

    override def getMaxTrials: Long = maxTrialsStop.getMaxTrials
    override def getMaxDuration: Duration = timedStop.getMaxDuration

    override def shouldStop(trialsSoFar: Long, durationSinceFirstTrial: Duration): Boolean = {
      maxTrialsStop.shouldStop(trialsSoFar, durationSinceFirstTrial) || timedStop.shouldStop(trialsSoFar, durationSinceFirstTrial)
    }
  }
}

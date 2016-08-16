package com.eharmony.spotz.examples

import org.rogach.scallop.ScallopConf

class Configuration(args: Array[String]) extends ScallopConf(args) {
  val numTrials = opt[Long](name = "numTrials", descr = "Number of trials", required = false)
  val duration = opt[Int](name = "duration", descr = "Number of seconds", required = false)
  val numBatchTrials = opt[Int](name = "numBatchTrials", descr = "Number of batch trials", short = 'b', default = Option(10), required = false)
  val seed = opt[Long](name = "seed", descr = "seed for random number generator", required = false, default = Option(0L))
}

trait RandomSearchConfiguration extends Configuration {
  validateOpt(duration, numTrials) {
    case (None, None) => Left("Must specify at least one of numTrials or duration")
    case _ => Right(Unit)
  }
}

trait GridSearchConfiguration extends Configuration {
  validateOpt(duration, numTrials) {
    case (None, None) => Right(Unit)
    case _ => Left("numTrials and numSeconds are not needed in a grid search")
  }
}
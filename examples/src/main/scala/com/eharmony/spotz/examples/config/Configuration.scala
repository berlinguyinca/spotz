package com.eharmony.spotz.examples.config

import com.eharmony.spotz.optimizer.OptimizerConstants._
import org.rogach.scallop.ScallopConf

/**
  * @author vsuthichai
  */
abstract class Configuration(args: Array[String]) extends ScallopConf(args) {
  val backend = opt[String](name = "backend", descr = "Distributed Computation Backend [spark|threads]",
    short = 'b', required = true, validate = s => Set(SPARK_BACKEND, THREADS_BACKEND).contains(s))
  val optimizer = opt[String](name = "optimizer", descr = "Optimization algorithm [random|grid]",
    short = 'o', required = true, validate = s => Set(RANDOM_SEARCH, GRID_SEARCH).contains(s))
  // TODO: Make this work for threads
  val parallelism = opt[String](name = "parallelism", descr = "Set parallelism",
    short = 'p', required = false)
  val duration = opt[Int](name = "duration", descr = "Duration in seconds",
    short = 'd', required = false)
  val trials = opt[Int](name = "trials", descr = "Number of trials",
    short = 't', required = false)

  // TODO: Add Validation
}

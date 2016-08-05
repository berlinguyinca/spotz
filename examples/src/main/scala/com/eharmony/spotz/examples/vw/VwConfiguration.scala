package com.eharmony.spotz.examples.vw

import com.eharmony.spotz.examples.config.Configuration
import com.eharmony.spotz.optimizer.OptimizerConstants._

/**
  * @author vsuthichai
  */
case class VwHyperParam(name: String)

class VwConfiguration(args: Array[String]) extends Configuration(args) {
  val trainParams = opt[String](name = "backend", descr = "Distributed Computation Backend [spark|threads]",
    short = 'b', required = true, validate = s => Set(SPARK_BACKEND, THREADS_BACKEND).contains(s))
  val hp = opt[List[String]](name = "hp", descr = "Hyper parameters")


}

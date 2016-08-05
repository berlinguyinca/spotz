package com.eharmony.spotz.examples.config

import com.eharmony.spotz.Preamble.Point
import com.eharmony.spotz.objective.Objective
import com.eharmony.spotz.optimizer._
import com.eharmony.spotz.optimizer.OptimizerConstants._
import com.eharmony.spotz.optimizer.grid.{ParGridSearch, SparkGridSearch}
import com.eharmony.spotz.optimizer.random.{ParRandomSearch, SparkRandomSearch}
import org.apache.spark.{SparkConf, SparkContext}
import org.joda.time.Duration

/**
  * @author vsuthichai
  */
object ExampleRunner {
  def stopStrategy(conf: Configuration): StopStrategy = {
    if (conf.trials.isDefined && !conf.duration.isDefined)
      StopStrategy.stopAfterMaxTrials(conf.trials())
    else if (!conf.trials.isDefined && conf.duration.isDefined)
      StopStrategy.stopAfterMaxDuration(Duration.standardSeconds(conf.duration()))
    else if (conf.trials.isDefined && conf.duration.isDefined)
      StopStrategy.stopAfterMaxTrialsOrMaxDuration(conf.trials(), Duration.standardSeconds(conf.duration()))
    else
      StopStrategy.stopWhenOptimizerFinishes
  }

  def apply(conf: Configuration,
            objective: Objective[Point, Double],
            space: ParameterSpaceArgs,
            minimize: Boolean): OptimizerResult[Point, Double] = {
    apply(objective, space, stopStrategy(conf),conf.optimizer(), conf.backend(), true)
  }

  def apply(objective: Objective[Point, Double],
            space: ParameterSpaceArgs,
            stopStrategy: StopStrategy,
            optimizerName: String,
            backendName: String,
            minimize: Boolean): OptimizerResult[Point, Double] = {
    (backendName, optimizerName) match {
      case (SPARK_BACKEND, RANDOM_SEARCH) =>
        val sc = new SparkContext(new SparkConf().setAppName("Spark Random Search Runner"))
        val optimizer = new SparkRandomSearch[Point, Double](sc, space.forRandomSearch, stopStrategy)
        val result = if (minimize) optimizer.minimize(objective) else optimizer.maximize(objective)
        sc.stop()
        result

      case (THREADS_BACKEND, RANDOM_SEARCH) =>
        val optimizer = new ParRandomSearch[Point, Double](space.forRandomSearch, stopStrategy)
        if (minimize) optimizer.minimize(objective) else optimizer.maximize(objective)

      case (SPARK_BACKEND, GRID_SEARCH) =>
        val sc = new SparkContext(new SparkConf().setAppName("Spark Grid Search Runner"))
        val optimizer = new SparkGridSearch[Point, Double](sc, space.forGridSearch)
        val result = if (minimize) optimizer.minimize(objective) else optimizer.maximize(objective)
        sc.stop()
        result

      case (THREADS_BACKEND, GRID_SEARCH) =>
        val optimizer = new ParGridSearch[Point, Double](space.forGridSearch)
        if (minimize) optimizer.minimize(objective) else optimizer.maximize(objective)
    }
  }
}

case class ParameterSpaceArgs(forGridSearch: Map[String, Iterable[_]] = Map[String, Iterable[_]](),
                              forRandomSearch: Map[String, RandomSampler[_]] = Map[String, RandomSampler[_]]())
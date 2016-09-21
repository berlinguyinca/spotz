package com.eharmony.spotz.examples

import com.eharmony.spotz.Preamble.Point
import com.eharmony.spotz.objective.Objective
import com.eharmony.spotz.optimizer.grid.{GridSearchResult, ParGridSearch, SparkGridSearch}
import com.eharmony.spotz.optimizer.random.{ParRandomSearch, RandomSearchResult, SparkRandomSearch}
import com.eharmony.spotz.optimizer.StopStrategy
import com.eharmony.spotz.optimizer.hyperparam.RandomSampler
import org.apache.spark.{SparkConf, SparkContext}

/**
  * @author vsuthichai
  */
trait ExampleRunner {
  def randomSearch(objective: Objective[Point, Double], params: Map[String, RandomSampler[_]], stop: StopStrategy, numBatchTrials: Int): RandomSearchResult[Point, Double]
  def gridSearch(objective: Objective[Point, Double], params: Map[String, Iterable[AnyVal]], numBatchTrials: Int): GridSearchResult[Point, Double]
}

trait ParExampleRunner extends ExampleRunner {
  override def randomSearch(objective: Objective[Point, Double], params: Map[String, RandomSampler[_]], stop: StopStrategy, numBatchTrials: Int) = {
    val optimizer = new ParRandomSearch[Point, Double](stop, numBatchTrials)
    optimizer.minimize(objective, params)
  }

  override def gridSearch(objective: Objective[Point, Double], params: Map[String, Iterable[AnyVal]], numBatchTrials: Int): GridSearchResult[Point, Double] = {
    val optimizer = new ParGridSearch[Point, Double](trialBatchSize = numBatchTrials)
    optimizer.minimize(objective, params)
  }
}

trait SparkExampleRunner extends ExampleRunner {
  val sc = new SparkContext(new SparkConf().setMaster("local[*]").setAppName("Spark Example Runner"))

  override def randomSearch(objective: Objective[Point, Double], params: Map[String, RandomSampler[_]], stop: StopStrategy, numBatchTrials: Int) = {
    val optimizer = new SparkRandomSearch[Point, Double](sc, stop, numBatchTrials)
    val result = optimizer.minimize(objective, params)
    sc.stop()
    result
  }

  override def gridSearch(objective: Objective[Point, Double], params: Map[String, Iterable[AnyVal]], numBatchTrials: Int) = {
    val optimizer = new SparkGridSearch[Point, Double](sc, trialBatchSize = numBatchTrials)
    val result = optimizer.minimize(objective, params)
    sc.stop()
    result
  }
}

trait GridSearchRunner extends ExampleRunner {
  val hyperParameters: Map[String, Iterable[AnyVal]]
  val objective: Objective[Point, Double]
  val numBatchTrials: Int

  def apply(): GridSearchResult[Point, Double] = gridSearch(objective, hyperParameters, numBatchTrials)
}

trait RandomSearchRunner extends ExampleRunner {
  val hyperParameters: Map[String, RandomSampler[_]]
  val objective: Objective[Point, Double]
  val stop: StopStrategy
  val numBatchTrials: Int

  def apply(): RandomSearchResult[Point, Double] = randomSearch(objective, hyperParameters, stop, numBatchTrials)
}

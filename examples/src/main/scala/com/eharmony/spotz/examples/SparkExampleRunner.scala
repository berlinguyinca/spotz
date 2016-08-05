package com.eharmony.spotz.examples

import com.eharmony.spotz.Preamble.Point
import com.eharmony.spotz.objective.Objective
import com.eharmony.spotz.optimizer.grid.{GridSearchResult, ParGridSearch, SparkGridSearch}
import com.eharmony.spotz.optimizer.random.{ParRandomSearch, RandomSearchResult, SparkRandomSearch}
import com.eharmony.spotz.optimizer.{RandomSampler, StopStrategy}
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
    val optimizer = new ParRandomSearch[Point, Double](params, stop, numBatchTrials)
    optimizer.minimize(objective)
  }

  override def gridSearch(objective: Objective[Point, Double], params: Map[String, Iterable[AnyVal]], numBatchTrials: Int): GridSearchResult[Point, Double] = {
    val optimizer = new ParGridSearch[Point, Double](params, numBatchTrials)
    optimizer.minimize(objective)
  }
}

trait SparkExampleRunner extends ExampleRunner {
  override def randomSearch(objective: Objective[Point, Double], params: Map[String, RandomSampler[_]], stop: StopStrategy, numBatchTrials: Int) = {
    val sc = new SparkContext(new SparkConf().setMaster("local[*]").setAppName("Spark Random Search"))
    val optimizer = new SparkRandomSearch[Point, Double](sc, params, stop, numBatchTrials)
    val result = optimizer.minimize(objective)
    sc.stop()
    result
  }

  override def gridSearch(objective: Objective[Point, Double], params: Map[String, Iterable[AnyVal]], numBatchTrials: Int) = {
    val sc = new SparkContext(new SparkConf().setMaster("local[*]").setAppName("Spark Grid Search"))
    val optimizer = new SparkGridSearch[Point, Double](sc, params, numBatchTrials)
    val result = optimizer.minimize(objective)
    sc.stop()
    result
  }
}

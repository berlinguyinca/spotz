package com.eharmony.spotz.examples.vw

import com.eharmony.spotz.Preamble._
import com.eharmony.spotz.objective.vw.VwHoldoutObjective
import com.eharmony.spotz.optimizer.grid.{GridSearch, GridSpace}
import com.eharmony.spotz.optimizer.StopStrategy
import com.eharmony.spotz.optimizer.random.RandomSearch
import com.eharmony.spotz.space.{HyperParameter, HyperSpace, Uniform, _}
import org.apache.spark.{SparkConf, SparkContext}

/**
  * @author vsuthichai
  */
object SparkVwHoldout {

  def randomSearch(args: Array[String]) = {
    val trials = args(1).toInt
    val vwTrainPath = args(2)
    val vwTestPath = args(3)

    val sc = new SparkContext(new SparkConf().setAppName("VW Optimization Example"))
    val stopStrategy = StopStrategy.stopAfterMaxTrials(trials)
    val optimizer = new RandomSearch[Point, Double](sc, stopStrategy)

    val objective = new VwHoldoutObjective(
      sc = sc,
      vwTrainSetPath = vwTrainPath,
      vwTrainParamsString = Option("--passes 10 --loss_function logistic"),
      vwTestSetPath = vwTestPath,
      vwTestParamsString = Option("--loss_function logistic"))

    val space = new HyperSpace(seed = 0, Seq(
      HyperParameter("l", new Uniform(0, 1))
    ))

    val result = optimizer.minimize(objective, space)
    sc.stop()
    result
  }

  def gridSearch(args: Array[String]) = {
    val trials = args(1).toInt
    val vwTrainPath = args(2)
    val vwTestPath = args(3)

    val sc = new SparkContext(new SparkConf().setAppName("VW Optimization Example"))
    val optimizer = new GridSearch[Point, Double](sc)

    val objective = new VwHoldoutObjective(
      sc = sc,
      vwTrainSetPath = vwTrainPath,
      vwTrainParamsString = Option("--passes 10 --loss_function logistic"),
      vwTestSetPath = vwTestPath,
      vwTestParamsString = Option("--loss_function logistic"))

    val space = new GridSpace(Seq(
      ("l", Seq(1,2,3))
    ))

    // Minimize
    val result = optimizer.minimize(objective, space)
    sc.stop()
    result
  }

  def main(args: Array[String]) {
    val result = args(0).toLowerCase match {
      case "random" => randomSearch(args)
      case "grid" => gridSearch(args)
    }
    println(result)
  }
}

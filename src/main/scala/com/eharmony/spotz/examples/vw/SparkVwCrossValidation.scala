package com.eharmony.spotz.examples.vw

import com.eharmony.spotz.Preamble
import Preamble._
import com.eharmony.spotz.objective.vw.VwCrossValidationObjective
import com.eharmony.spotz.optimizer.grid.{GridSearch, GridSpace}
import com.eharmony.spotz.optimizer.{StopStrategy, UniformDouble}
import com.eharmony.spotz.optimizer.random.{RandomSearch, RandomSpace}
import org.apache.spark.{SparkConf, SparkContext}

/**
  * @author vsuthichai
  */
object SparkVwCrossValidation {

  def randomSearch(args: Array[String]) = {
    val trials = args(1).toInt
    val folds = args(2).toInt
    val vwDataset = args(3)

    val sc = new SparkContext(new SparkConf().setAppName("VW Optimization Example"))
    val stopStrategy = StopStrategy.stopAfterMaxTrials(trials)
    val optimizer = new RandomSearch[Point, Double](sc, stopStrategy)

    val objective = new VwCrossValidationObjective(
      sc = sc,
      numFolds = folds,
      vwDatasetPath = vwDataset,
      vwTrainParamsString = Option("--passes 10 --loss_function logistic"),
      vwTestParamsString = Option("--loss_function logistic"))

    val space = new RandomSpace[Point](Map(
      ("l",  new UniformDouble(0, 1)),
      ("l2", new UniformDouble(0, 0.2))
    ))

    val result = optimizer.minimize(objective, space)
    sc.stop()
    result
  }

  def gridSearch(args: Array[String]) = {
    val trials = args(1).toInt
    val folds = args(2).toInt
    val vwDataset = args(3)

    val sc = new SparkContext(new SparkConf().setAppName("VW Optimization Example"))
    val optimizer = new GridSearch[Point, Double](sc)

    val objective = new VwCrossValidationObjective(
      sc = sc,
      numFolds = folds,
      vwDatasetPath = vwDataset,
      vwTrainParamsString = Option("--passes 10 --loss_function logistic"),
      vwTestParamsString = Option("--loss_function logistic"))

    val space = new GridSpace[Point](Map(
      ("l", 1 to 10)
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

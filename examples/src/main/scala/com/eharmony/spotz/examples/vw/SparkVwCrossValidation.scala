package com.eharmony.spotz.examples.vw

import com.eharmony.spotz.Preamble
import Preamble._
import com.eharmony.spotz.objective.vw._
import com.eharmony.spotz.optimizer.grid.{GridSearch, GridSpace, ParGridSearch}
import com.eharmony.spotz.optimizer.{StopStrategy, UniformDouble}
import com.eharmony.spotz.optimizer.random.{RandomSearch, RandomSpace, SparkRandomSearch}

/**
  * @author vsuthichai
  */
abstract class BaseVwCrossValidation {
  def randomSearch(trials: Int, folds: Int, vwDataset: String) {

  }

  def randomSearch(args: Array[String]) = {
    val trials = args(1).toInt
    val folds = args(2).toInt
    val vwDataset = args(3)

    import org.apache.spark.{SparkConf, SparkContext}

    val sc = new SparkContext(new SparkConf().setAppName("VW Optimization Example"))
    val stopStrategy = StopStrategy.stopAfterMaxTrials(trials)
    val optimizer = new SparkRandomSearch[Point, Double](sc, stopStrategy)

    val objective = new SparkVwCrossValidationObjective(
      sc = sc,
      numFolds = folds,
      vwDatasetPath = vwDataset,
      vwTrainParamsString = Option("--passes 10 --loss_function logistic"),
      vwTestParamsString = Option("--loss_function logistic")
    )

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
/*
    import org.apache.spark.{SparkConf, SparkContext}

    val sc = new SparkContext(new SparkConf().setAppName("VW Optimization Example"))

    val optimizer = new GridSearch[Point, Double]() with SparkFunctions {
      @transient val sparkContext = sc
    }
    */
    val optimizer = new ParGridSearch[Point, Double]()

    val objective = new VwCrossValidationObjective(
      numFolds = folds,
      vwDatasetPath = vwDataset,
      vwTrainParamsString = Option("--passes 10 --loss_function logistic"),
      vwTestParamsString = Option("--loss_function logistic"))

    val space = new GridSpace[Point](Map(
      ("l", 1 to 3),
      ("l1", Seq(0.001, 0.1))
    ))

    // Minimize
    val result = optimizer.minimize(objective, space)
    //sc.stop()
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

object VwCrossValidation {
  def randomSearch(args: Array[String]) = {

  }
}
/*
object SparkVwCrossValidation {
  import org.apache.spark.{SparkConf, SparkContext}

  def randomSearch(args: Array[String]) = {
    val sc = new SparkContext(new SparkConf()().setAppName("Random Search"))
    val optimizer = new RandomSearch[Point, Double]()
  }
}*/
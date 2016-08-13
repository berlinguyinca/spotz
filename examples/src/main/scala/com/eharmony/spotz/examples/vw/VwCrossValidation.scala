package com.eharmony.spotz.examples.vw

import com.eharmony.spotz.Preamble
import Preamble._
import com.eharmony.spotz.examples.{ExampleRunner, GridSearchRunner}
import com.eharmony.spotz.objective.Objective
import com.eharmony.spotz.objective.vw._
import com.eharmony.spotz.optimizer.grid.{GridSearchResult, Grid, ParGridSearch}
import com.eharmony.spotz.optimizer.{RandomSampler, StopStrategy, UniformDouble}

/**
  * @author vsuthichai
  */
/*
object VwCrossValidation {
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

    val space = Map(
      ("l",  UniformDouble(0, 1)),
      ("l2", UniformDouble(0, 0.2))
    )

    val result = optimizer.minimize(objective, space)
    sc.stop()
    result
  }

  def gridSearch(args: Array[String]) = {
    val trials = args(1).toInt
    val folds = args(2).toInt
    val vwDataset = args(3)

    import org.apache.spark.{SparkConf, SparkContext}

    val sc = new SparkContext(new SparkConf().setAppName("VW Optimization Example"))

    val optimizer = new GridSearch[Point, Double]() with SparkFunctions {
      @transient val sparkContext = sc
    }

    val optimizer = new ParGridSearch[Point, Double]()

    val objective = new VwCrossValidationObjective(
      numFolds = folds,
      vwDatasetPath = vwDataset,
      vwTrainParamsString = Option("--passes 10 --loss_function logistic"),
      vwTestParamsString = Option("--loss_function logistic"))

    val space = Map(
      ("l", 1 to 3),
      ("l1", Seq(0.001, 0.1))
    )

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
*/
/*
trait VwCrossValidation {

}

trait VwCrossValidationGridSearch extends ExampleRunner {
  val hyperParmeters = Map(
    ("l", Range.Double(0.0, 1.0, 0.1))
  )

  val objective =
}

trait VwCrossValidationRandomSearch extends ExampleRunner {

}

object VwCrossValidationSparkGridSearch extends VwCrossValidationGridSearch with GridSearchRunner {
  override val hyperParameters: Map[String, Iterable[AnyVal]] = _
  override val objective: Objective[Point, Double] = _
  override val numBatchTrials: Int = _

  override def randomSearch(objective: Objective[Point, Double], params: Map[String, RandomSampler[_]], stop: StopStrategy, numBatchTrials: Int): RandomSearchResult[Point, Double] = ???

  override def gridSearch(objective: Objective[Point, Double], params: Map[String, Iterable[AnyVal]], numBatchTrials: Int): GridSearchResult[Point, Double] = ???
}
object VwCrossValidationParGridSearch
object VwCrossValidationSparkRandomSearch
object VwCrossValidationParRandomSearch
*/
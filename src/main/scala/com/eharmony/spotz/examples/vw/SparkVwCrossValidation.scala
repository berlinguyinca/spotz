package com.eharmony.spotz.examples.vw

import com.eharmony.spotz.Preamble
import Preamble._
import com.eharmony.spotz.objective.vw.VwCrossValidationObjective
import com.eharmony.spotz.optimizer.RandomSearch
import com.eharmony.spotz.optimizer.stop.StopStrategy
import com.eharmony.spotz.space.{HyperParameter, HyperSpace, Point, Uniform}
import org.apache.spark.{SparkConf, SparkContext}

/**
 * @author vsuthichai
 */
object SparkVwCrossValidation {

  def main(args: Array[String]) {
    val trials = args(0).toInt
    val folds = args(1).toInt
    val vwDataset = args(2)

    // Boiler plate
    val sc = new SparkContext(new SparkConf().setAppName("VW Optimization Example"))
    val stopStrategy = StopStrategy.stopAfterMaxTrials(trials)
    val optimizer = new RandomSearch[Point, Double](sc, stopStrategy)
    val objective = new VwCrossValidationObjective(
      sc = sc,
      numFolds = folds,
      vwInputPath = vwDataset,
      vwTrainParamsString = Option("--passes 10 --loss_function logistic"),
      vwTestParamsString = Option("--loss_function logistic"))
    val space = new HyperSpace(seed = 0, Seq(
      HyperParameter("l", new Uniform(0, 1))
    ))

    // Minimize
    val result = optimizer.minimize(objective, space)

    sc.stop()
    println(result)
  }

}

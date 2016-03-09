package com.eharmony.spotz.examples.vw

import com.eharmony.spotz.Preamble
import Preamble._
import com.eharmony.spotz.optimizer.RandomSearch
import com.eharmony.spotz.objective.vw.SparkVwCrossValidationObjective
import com.eharmony.spotz.optimizer.framework.SparkFramework
import com.eharmony.spotz.optimizer.stop.StopStrategy
import com.eharmony.spotz.space.{Uniform, HyperParameter, HyperSpace, Point}
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
    val framework = new SparkFramework[Point, Double](sc)
    val stopStrategy = StopStrategy.stopAfterMaxTrials(trials)
    val optimizer = new RandomSearch[Point, Double](framework, stopStrategy)
    val objective = new SparkVwCrossValidationObjective(sc, folds, vwDataset, "--binary")
    val space = new HyperSpace(seed = 0, Seq(
      HyperParameter("l", new Uniform(0, 20))
    ))

    // Minimize
    val result = optimizer.minimize(objective, space)

    sc.stop()
    println(result)
  }

}

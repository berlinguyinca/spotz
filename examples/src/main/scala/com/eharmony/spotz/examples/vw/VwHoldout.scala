package com.eharmony.spotz.examples.vw

import com.eharmony.spotz.Preamble._
import com.eharmony.spotz.objective.vw.VwHoldoutObjective
import com.eharmony.spotz.optimizer.grid.{GridSpace, ParGridSearch}
import com.eharmony.spotz.optimizer.random.{ParRandomSearch, RandomSpace}
import com.eharmony.spotz.optimizer.{StopStrategy, UniformDouble}

/**
  * @author vsuthichai
  */
/*
object VwHoldout {

  def randomSearch(args: Array[String]) = {
    val trials = args(1).toInt
    val vwTrainPath = args(2)
    val vwTestPath = args(3)

    val stopStrategy = StopStrategy.stopAfterMaxTrials(trials)
    val optimizer = new ParRandomSearch[Point, Double](stopStrategy)

    val objective = new VwHoldoutObjective(
      vwTrainSetPath = vwTrainPath,
      vwTrainParamsString = Option("--passes 10 --loss_function logistic"),
      vwTestSetPath = vwTestPath,
      vwTestParamsString = Option("--loss_function logistic")
    )

    val space = new RandomSpace[Point](Map(
      ("l", new UniformDouble(0, 1))
    ))

    val result = optimizer.minimize(objective, space)
    result
  }

  def gridSearch(args: Array[String]) = {
    val trials = args(1).toInt
    val vwTrainPath = args(2)
    val vwTestPath = args(3)

    val optimizer = new ParGridSearch[Point, Double]()
    val objective = new VwHoldoutObjective(
      vwTrainSetPath = vwTrainPath,
      vwTrainParamsString = Option("--passes 10 --loss_function logistic"),
      vwTestSetPath = vwTestPath,
      vwTestParamsString = Option("--loss_function logistic")
    )

    val space = new GridSpace[Point](Map(
      ("l", Seq(1,2,3))
    ))

    // Minimize
    val result = optimizer.minimize(objective, space)
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
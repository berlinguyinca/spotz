package com.eharmony.spotz.examples.ackley

import com.eharmony.spotz.Preamble.Point
import com.eharmony.spotz.optimizer.grid.{GridSearchResult, ParGridSearch}
import com.eharmony.spotz.optimizer.random.{ParRandomSearch, RandomSearchResult, SparkRandomSearch}
import com.eharmony.spotz.optimizer.{OptimizerResult, StopStrategy, UniformDouble}
import org.apache.spark.{SparkConf, SparkContext}
import org.joda.time.Duration


/**
  * @author vsuthichai
  */
trait AckleyExample {
  val objective = new AckleyObjective
  val stop = StopStrategy.stopAfterMaxDuration(Duration.standardSeconds(5))
  val numBatchTrials = 1000000

  def apply(): OptimizerResult[Point, Double]

  def main(args: Array[String]) {
    val result = apply()
    println(result)
  }
}

trait AckleyRandomSearch extends AckleyExample {
  val hyperParameters = Map(
    ("x", UniformDouble(-5, 5)),
    ("y", UniformDouble(-5, 5))
  )
}

trait AckleyGridSearch extends AckleyExample {
  val hyperParameters = Map(
    ("x", Range.Double(-5, 5, 0.01)),
    ("y", Range.Double(-5, 5, 0.01))
  )
}

object AckleyParGridSearch extends AckleyGridSearch {
  override def apply(): GridSearchResult[Point, Double] = {
    val optimizer = new ParGridSearch[Point, Double](hyperParameters, numBatchTrials)
    optimizer.minimize(objective)
  }
}

object AckleyParRandomSearch extends AckleyRandomSearch {
  override def apply(): RandomSearchResult[Point, Double] = {
    val optimizer = new ParRandomSearch[Point, Double](hyperParameters, stop, numBatchTrials)
    optimizer.minimize(objective)
  }
}

object AckleySparkGridSearch extends AckleyGridSearch {
  override def apply(): GridSearchResult[Point, Double] = {
    val sc = new SparkContext(new SparkConf().setMaster("local[*]").setAppName("Ackley Spark Grid Search"))
    val optimizer = new ParGridSearch[Point, Double](hyperParameters, numBatchTrials)
    val result = optimizer.minimize(objective)
    sc.stop()
    result
  }
}

object AckleySparkRandomSearch extends AckleyRandomSearch {
  override def apply(): RandomSearchResult[Point, Double] = {
    val sc = new SparkContext(new SparkConf().setMaster("local[*]").setAppName("Ackley Spark Random Search"))
    val optimizer = new SparkRandomSearch[Point, Double](sc, hyperParameters, stop, numBatchTrials)
    val result = optimizer.minimize(objective)
    sc.stop()
    result
  }
}

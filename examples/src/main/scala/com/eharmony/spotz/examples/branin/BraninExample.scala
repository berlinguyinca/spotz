package com.eharmony.spotz.examples.branin

import com.eharmony.spotz.Preamble.Point
import com.eharmony.spotz.optimizer.grid.{GridSearchResult, ParGridSearch}
import com.eharmony.spotz.optimizer.random.{ParRandomSearch, RandomSearchResult, SparkRandomSearch}
import com.eharmony.spotz.optimizer.{OptimizerResult, StopStrategy, UniformDouble}
import org.apache.spark.{SparkConf, SparkContext}
import org.joda.time.Duration


/**
  * @author vsuthichai
  */
trait BraninExample {
  val objective = new BraninObjective
  val stop = StopStrategy.stopAfterMaxDuration(Duration.standardSeconds(5))
  val numBatchTrials = 1000000

  def apply(): OptimizerResult[Point, Double]

  def main(args: Array[String]) {
    val result = apply()
    println(result)
  }
}

trait BraninRandomSearch extends BraninExample {
  val hyperParameters = Map(
    ("x1", UniformDouble(-5, 10)),
    ("x2", UniformDouble(0, 15))
  )
}

trait BraninGridSearch extends BraninExample {
  val hyperParameters = Map(
    ("x1", Range.Double(-5, 10, 0.01)),
    ("x2", Range.Double(0, 15, 0.01))
  )
}

object BraninParGridSearch extends BraninGridSearch {
  override def apply(): GridSearchResult[Point, Double] = {
    val optimizer = new ParGridSearch[Point, Double](hyperParameters, numBatchTrials)
    optimizer.minimize(objective)
  }
}

object BraninParRandomSearch extends BraninRandomSearch {
  override def apply(): RandomSearchResult[Point, Double] = {
    val optimizer = new ParRandomSearch[Point, Double](hyperParameters, stop, numBatchTrials)
    optimizer.minimize(objective)
  }
}

object BraninSparkGridSearch extends BraninGridSearch {
  override def apply(): GridSearchResult[Point, Double] = {
    val sc = new SparkContext(new SparkConf().setMaster("local[*]").setAppName("Branin Spark Grid Search"))
    val optimizer = new ParGridSearch[Point, Double](hyperParameters, numBatchTrials)
    val result = optimizer.minimize(objective)
    sc.stop()
    result
  }
}

object BraninSparkRandomSearch extends BraninRandomSearch {
  override def apply(): RandomSearchResult[Point, Double] = {
    val sc = new SparkContext(new SparkConf().setMaster("local[*]").setAppName("Branin Spark Random Search"))
    val optimizer = new SparkRandomSearch[Point, Double](sc, hyperParameters, stop, numBatchTrials)
    val result = optimizer.minimize(objective)
    sc.stop()
    result
  }
}

package com.eharmony.spotz.examples.branin

import com.eharmony.spotz.Preamble.Point
import com.eharmony.spotz.optimizer.StopStrategy
import com.eharmony.spotz.optimizer.grid.{ParGridSearch, SparkGridSearch}
import com.eharmony.spotz.optimizer.random.{ParRandomSearch, SparkRandomSearch}
import org.apache.spark.{SparkConf, SparkContext}
import org.junit.Assert._
import org.junit.Test

/**
  * @author vsuthichai
  */
class BraninTest {
  @Test
  def testBraninParRandomSearch() {
    val paramSpace = BraninMain.paramsForRandomSearch
    val stopStrategy = StopStrategy.stopAfterMaxTrials(1000000)
    val optimizer = new ParRandomSearch[Point, Double](paramSpace, stopStrategy)
    val result = optimizer.minimize(new BraninObjective)
    assertEquals(result.bestLoss, 0.397887, 0.001)
  }

  @Test
  def testBraninParGridSearch() {
    val paramSpace = BraninMain.paramsForGridSearch
    val optimizer = new ParGridSearch[Point, Double](paramSpace)
    val result = optimizer.minimize(new BraninObjective)
    assertEquals(result.bestLoss, 0.397887, 0.001)
  }

  @Test
  def testBraninSparkRandomSearch() {
    val sc = new SparkContext(new SparkConf().setMaster("local[*]").setAppName("Branin Spark Random Search"))
    val paramSpace = BraninMain.paramsForRandomSearch
    val stopStrategy = StopStrategy.stopAfterMaxTrials(1000000)
    val optimizer = new SparkRandomSearch[Point, Double](sc, paramSpace, stopStrategy)
    val result = optimizer.minimize(new BraninObjective)
    sc.stop()
    assertEquals(result.bestLoss, 0.397887, 0.001)
  }

  @Test
  def testBraninSparkGridSearch() {
    val sc = new SparkContext(new SparkConf().setMaster("local[*]").setAppName("Branin Grid Random Search"))
    val paramSpace = BraninMain.paramsForGridSearch
    val optimizer = new SparkGridSearch[Point, Double](sc, paramSpace)
    val result = optimizer.minimize(new BraninObjective)
    sc.stop()
    assertEquals(result.bestLoss, 0.397887, 0.001)
  }
}

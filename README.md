# spotz [![Build Status](https://travis-ci.org/eHarmony/spotz.svg?branch=master)](https://travis-ci.org/eHarmony/spotz) [![Stories in Ready](https://badge.waffle.io/eHarmony/spotz.png?label=ready&title=Ready)](https://waffle.io/eHarmony/spotz) #
# Spark Parameter Optimization

Spotz is a hyperparameter optimization framework designed to exploit Apache
Spark to perform its distributed computation.  A broad set of optimization
algorithms have been implemented to solve for the hyperparameter values
of an objective function that you specify.

## Objective Function Trait

Defining your own objective function

```scala
import com.eharmony.spotz.Preamble.Point
import com.eharmony.spotz.objective.Objective

import scala.math._

class BraninObjective[P <: Point] extends Objective[P, Double] {
  val a = 1
  val b = 5.1 / (4 * pow(Pi, 2))
  val c = 5 / Pi
  val r = 6
  val s = 10
  val t = 1 / (8 * Pi)

 /**
   *  Input Domain:
   *  This function is usually evaluated on the square x1 ∈ [-5, 10], x2 ∈ [0, 15].
   *
   *  Global Minimum:
   *  f(x*) = 0.397887 at x* = (-Pi, 12.275), (Pi, 2.275), (9.42478, 2.475)
   *
   * @param point
   * @return a Double which is the result of evaluating the Branin function
   */
  override def apply(point: P): Double = {
    val x1 = point.get[Double]("x1")
    val x2 = point.get[Double]("x2")

    a * pow(x2 - b*pow(x1, 2) + c*x1 - r, 2) + s*(1-t)*cos(x1) + s
  }
}


## Boilerplate
```scala
import com.eharmony.spotz.Preamble._
import com.eharmony.spotz.optimizer.StopStrategy
import com.eharmony.spotz.optimizer.random.{RandomSearch, RandomSpace, Uniform}
import org.apache.spark.{SparkConf, SparkContext}

val sc = new SparkContext(new SparkConf().setAppName("Branin Function Trials"))
val space = new RandomSpace[Point](Map(
  ("x1", new Uniform(-5, 10),
  ("x2", new Uniform(0, 15)
)
val stopStrategy = StopStrategy.stopAfterMaxTrials(maxTrials)
val optimizer = new RandomSearch[Point, Double](sc, stopStrategy)
val result = optimizer.minimize(new BraninObjective, space)
```

# spotz [![Build Status](https://travis-ci.org/eHarmony/spotz.svg?branch=master)](https://travis-ci.org/eHarmony/spotz) [![Stories in Ready](https://badge.waffle.io/eHarmony/spotz.png?label=ready&title=Ready)](https://waffle.io/eHarmony/spotz) #
# Spark Parameter Optimization

Spotz is a hyperparameter optimization framework written in Scala designed
to exploit Apache Spark to perform its distributed computation.  A broad
set of optimization algorithms have been implemented to solve for the
hyperparameter values of an objective function that you specify.

## Optimization Algorithms

Currently the following solvers have been implemented:

* [Random Search](https://en.wikipedia.org/wiki/Random_search)
* [Grid Search](https://en.wikipedia.org/wiki/Grid_search_method)

## Usage

Using this framework consists of writing the following boilerplate code:

1. Define the objective function
2. Define the space of hyperparameter values that you wish to search.
3. Selecting the solver.

## Objective Function Trait

Define your objective function by implementing the ```Objective[P, L]```
trait.

```scala
trait Objective[P, L]  {
  def apply(point: P): L
}
```

Note that the objective function trait is type parameterized ```[P, L]``` for
the point and the loss.  This function must simply implement the
```apply(point: P): L``` method of that trait.  The point type parameter is an
abstract representation of the current hyperparameter values and is passed
into the trait through the apply method.  The loss is the value returned from
executing the objective function.  The framework default implementation
provides a ```Point``` class for the ```P``` type parameter and uses
```Double``` as the loss value.

The Branin-Hoo function is shown here as a test object function
example.  Read more about it here: <http://www.sfu.ca/~ssurjano/branin.html>.

```scala
class BraninObjective extends Objective[Point, Double] {
  val a = 1
  val b = 5.1 / (4 * pow(Pi, 2))
  val c = 5 / Pi
  val r = 6
  val s = 10
  val t = 1 / (8 * Pi)

 /**
   *  Input Domain:
   *  This function is usually evaluated on
   *  x1 ∈ [-5, 10], x2 ∈ [0, 15].
   *
   *  Global Minimum:
   *  f(x*) = 0.397887 at x* = (-Pi, 12.275), (Pi, 2.275), (9.42478, 2.475)
   *
   * @param point
   * @return a Double which is the result of evaluating the Branin function
   */
  override def apply(point: Point): Double = {
    val x1 = point.get[Double]("x1")
    val x2 = point.get[Double]("x2")

    a * pow(x2 - b*pow(x1, 2) + c*x1 - r, 2) + s*(1-t)*cos(x1) + s
  }
}
```

## Hyperparameter Space

Define the space of hyperparameter values that you desire to search.  TODO

## Choose Solver

Select the algorithm of your choice to perform the optimization.  Some
algorithms may require defining a stopping strategy.  This states when you'd
like the solver to stop searching for the best hyperparameter values.

```scala
val stopStrategy = StopStrategy.stopAfterMaxTrials(maxTrials)
val optimizer = new RandomSearch[Point, Double](sparkContext, stopStrategy)
```

### Stop Strategies

Currently, there are few ways to specify stopping criteria:

* Stopping after maximum time duration:
```scala
StopStrategy.stopAfterMaxDuration(maxDuration)
```

* Stopping after maximum number of trials:
```scala
StopStrategy.stopAfterMaxTrials(maxTrials)
```

* Stopping after a maximum number of trials or a maximum time duration:
```scala
StopStrategy.stopAfterMaxTrialsOrMaxDuration(maxTrials, maxDuration)
```

## Full Example

Wiring it all together, here is all the necessary boilerplate to make your
example work.

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

  override def apply(point: P): Double = {
    val x1 = point.get[Double]("x1")
    val x2 = point.get[Double]("x2")

    a * pow(x2 - b*pow(x1, 2) + c*x1 - r, 2) + s*(1-t)*cos(x1) + s
  }
}
```

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
sc.stop()
```

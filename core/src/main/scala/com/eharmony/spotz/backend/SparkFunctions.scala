package com.eharmony.spotz.backend

import com.eharmony.spotz.objective.Objective
import com.eharmony.spotz.optimizer.grid.GridSpace
import com.eharmony.spotz.optimizer.random.RandomSpace
import org.apache.spark.SparkContext

import scala.reflect.ClassTag

/**
  * @author vsuthichai
  */
trait SparkFunctions extends BackendFunctions {
  @transient val sc: SparkContext

  /**
    * The strategy for doing this is to generate the points by sampling from the space
    * for each partition.  The downside to doing this is that the rng within the space
    * object is identical across all the executors after the space object is serialized
    * to all the executors.  It causes the unintended side effect of every executor's
    * space object generating the SAME pseudo random numbers, so we need to figure out
    * a way to have every executor have its own unique rng object.
    *
    * This method samples batchSize points.  Each partition of trials has its own
    * space with unique rng to sample from so that any points generated from one
    * partition of trials is highly unlikely to be the same set of points generated
    * from another partition.
    *
    * An alternative to doing this is to generate the points on the driver and
    * have the points be serialized to the executors, but this approach has a lot
    * of network overhead, though it will fix the rng problem because there's only
    * a single space from which to sample the points.
    *
    * @param batchSize the number of points to sample from the space
    * @param objective the function on which a point is applied
    * @param space the space object from which to sample the points
    * @param reducer the reducer function to use on all the various points and losses
    *                generated
    * @return the best point with the best loss
    */
  protected override def bestRandomPointAndLoss[P, L](startIndex: Long,
                                                      batchSize: Long,
                                                      objective: Objective[P, L],
                                                      space: RandomSpace[P],
                                                      reducer: ((P, L), (P, L)) => (P, L)): (P, L) = {
    assert(batchSize > 0, "batchSize must be greater than 0")

    val rdd = sc.parallelize(startIndex until (startIndex + batchSize))

    val pointAndLossRDD = rdd.mapPartitions { partition =>
      partition.map { trial =>
        // Create new space with new seed to avoid every executor having the same rng state.
        val rngModifiedSpace = space.setSeed(space.seed + trial)
        val point = rngModifiedSpace.sample
        (point, objective(point))
      }
    }

    pointAndLossRDD.reduce(reducer)
  }

  protected override def bestGridPointAndLoss[P, L](startIndex: Long,
                                                    batchSize: Long,
                                                    objective: Objective[P, L],
                                                    space: GridSpace[P],
                                                    reducer: ((P, L), (P, L)) => (P, L))
                                                    (implicit c: ClassTag[P], p: ClassTag[L]): (P, L) = {
    val rdd = sc.parallelize(startIndex until (startIndex + batchSize))
    val pointAndLossRDD = rdd.map { case idx =>
      val point = space(idx)
      (point, objective(point))
    }

    pointAndLossRDD.reduce(reducer)
  }
}

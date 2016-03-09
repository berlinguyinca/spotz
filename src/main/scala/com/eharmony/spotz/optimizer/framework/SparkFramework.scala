package com.eharmony.spotz.optimizer.framework

import com.eharmony.spotz.optimizer.Reducer
import com.eharmony.spotz.objective.Objective
import com.eharmony.spotz.space.Space
import org.apache.spark.SparkContext

import scala.math.Ordering

/**
 * @author vsuthichai
 */
class SparkFramework[P, L](
    @transient sc: SparkContext)
    (implicit pointLossOrdering: Ordering[(P, L)]) extends Framework[P, L] {

  /**
   * This may need to be revisited.  The strategy for doing this is to generate
   * the points by sampling from the space on the executor.  The downside to doing
   * this is that the rng within the space object is identical across all the
   * executors after the space object is serialized to all the executors.  It
   * causes the unintended side effect of every executor's splace object generating
   * the SAME
   * pseudo random numbers, so we need to figure out a way to have every
   * executor have its own unique rng object.
   *
   * An alternative to doing this is to generate the points on the driver and
   * have the points be serialized to the executors, but this approach has a lot
   * of network overhead, though it will fix the rng problem because there's only
   * a single space from which to sample the points.
   *
   * @param objective
   * @param space
   * @param func
   * @return
   */
   override def bestRandomPoint(batchSize: Int,
                                objective: Objective[P, L],
                                space: Space[P],
                                func: Reducer[(P, L)]): (P, L) = {
     sc.parallelize(Seq(0 to batchSize - 1)).mapPartitions { partition =>
       // Create new space with new seed to avoid every executor having the same rng state.
       val rngModifiedSpace = space.seed(new scala.util.Random().nextLong())

       partition.map { trials =>
         trials.map { trial =>
           val point = rngModifiedSpace.sample
           (point, objective(point))
         }.reduce(func)
       }
     }.reduce(func)
  }
}

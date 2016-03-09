package com.eharmony.spotz.optimizer.framework

import com.eharmony.spotz.optimizer.Reducer
import com.eharmony.spotz.objective.Objective
import com.eharmony.spotz.space.Space

/**
 * This trait represents the computation framework responsible for parallelizing the work
 * of the optimization algorithms.
 *
 * @author vsuthichai
 */
trait Framework[P,L] extends Serializable {
  def bestRandomPoint(batchSize: Int, objective: Objective[P, L], space: Space[P], func: Reducer[(P, L)]): (P, L)
}

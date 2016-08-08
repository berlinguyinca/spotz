package com.eharmony.spotz.objective

/**
  * @author vsuthichai
  */
trait Objective[P, L] extends Serializable {
  def apply(point: P): L
}

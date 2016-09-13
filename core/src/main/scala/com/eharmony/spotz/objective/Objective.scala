package com.eharmony.spotz.objective

/**
  * The base trait from which all objective functions must inherit.  A point P is a representation
  * of hyper parameter values that the objective function will on.  The default implementation of P
  * is the Point class that is imported through the spotz package object.  This Point class is used
  * as a default throughout the entire Spotz library.
  *
  * @author vsuthichai
  */
trait Objective[P, L] extends Serializable {
  def apply(point: P): L
}

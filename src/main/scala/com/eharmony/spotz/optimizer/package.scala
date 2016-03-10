package com.eharmony.spotz

/**
 * @author vsuthichai
 */
package object optimizer {
  type Reducer[T] = (T, T) => T
}

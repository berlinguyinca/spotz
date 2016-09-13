import com.eharmony.spotz.optimizer.hyperparam.RandomSampler

import scala.util.Random

/**
  * Sample from a normal distribution given the mean and standard deviation
  *
  * {{{
  *   val hyperParamSpace = Map(
  *     ("x1", NormalDistribution(0, 0.1))
  *   )
  * }}}
  *
  * @param mean mean
  * @param std standard deviation
  */
case class NormalDistribution(mean: Double, std: Double) extends RandomSampler[Double] {
  override def apply(rng: Random): Double = {
    std * rng.nextGaussian() + mean
  }
}

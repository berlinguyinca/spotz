package com.eharmony.spotz.objective.vw

import com.eharmony.spotz.util.{ProcessResult, CommandLineProcess}
import com.eharmony.spotz.util.RegexUtil.floatingPointRegex

/**
 * @author vsuthichai
 */
case class VwProcess(params: String) extends CommandLineProcess {
  val cmd = s"vw $params"

  def apply(): VwResult = {
    val processResult: ProcessResult = run(cmd)

    VwResult(processResult.exitCode,
             processResult.stdout,
             processResult.stderr,
             averageLoss(processResult.stderr))
  }

  private[this] def averageLoss(stderr: String): Option[Double] = {
    VwProcess.avgLossRegex.findFirstMatchIn(stderr).map(_.group(1).toDouble)
  }

  // TODO: What else is important that should be returned as part of the VwResult?

  override def toString = cmd
}

case class VwResult(
    exitCode: Int,
    stdout: String,
    stderr: String,
    loss: Option[Double])

object VwProcess {
  val avgLossRegex = s"average\\s+loss\\s+=\\s+($floatingPointRegex)".r
}

/**
 * This is a builder class to make it more convenient to construct the VW command line
 * with all its various arguments.  Using this is not necessary at all as one could simply
 * specify their own VW parameters if they're familiar with them and pass them
 * as a string to the class constructor of VwProcess.
 */
class VwProcessBuilder {
  private[this] val sb = new StringBuilder

  private[this] def append(param: String): this.type = {
    sb.append(param)
    this
  }

  def params(params: String) = append(s"$params ")

  // TODO: Finish this builder up.  There are many parameters not here.
  def contextualBandits(cb: Int) = append(s"--cb $cb")
  def test() = append(s"-t")
  def binary() = append(s"--binary")
  def dataset(path: String) = append(s"-d $path")
  def learningRate(learningRate: Double) = append(s"-l $learningRate")
  def passes(passes: Int) = append(s"--passes $passes")
  def cache(cacheFile: String) = append(s"--cache_file $cacheFile")
  def quiet() = append("--quiet")
  def saveRegressorModel(path: String) = append(s"-f $path")
  def inputRegressorModel(path: String) = append(s"-i $path")

  def build = VwProcess(sb.toString())
  def execute = build()

  override def toString = s"vw ${sb.toString()}"
}

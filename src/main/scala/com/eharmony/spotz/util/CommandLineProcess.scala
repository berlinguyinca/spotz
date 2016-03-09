package com.eharmony.spotz.util

import scala.sys.process._

/**
 *
 * @author vsuthichai
 */
trait CommandLineProcess extends Serializable {
  // TODO: Enhance to support stdin later
  private[this] val stdoutBuffer = new StringBuilder
  private[this] val stderrBuffer = new StringBuilder
  private[this] val processLogger = ProcessLogger(line => stdoutBuffer.append(line).append("\n"),
                                                  line => stderrBuffer.append(line).append("\n"))

  def run(cmd: String): ProcessResult = {
    val exitCode = cmd ! processLogger
    val stdoutStr = stdoutBuffer.toString()
    val stderrStr = stderrBuffer.toString()

    stdoutBuffer.clear()
    stderrBuffer.clear()

    ProcessResult(exitCode, stdoutStr, stderrStr)
  }
}

case class ProcessResult(exitCode: Int, stdout: String, stderr: String)

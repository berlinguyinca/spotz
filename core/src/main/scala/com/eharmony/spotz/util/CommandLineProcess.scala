package com.eharmony.spotz.util

import scala.sys.process._

/**
  * Execute a command line process.
  *
  * @param cmd the command line to execute
  * @param stdin standard input
  */
class CommandLineProcess(cmd: String, stdin: Option[Iterator[String]]) extends Serializable {
  private val stdoutBuffer = new StringBuilder
  private val stderrBuffer = new StringBuilder
  private val processLogger = ProcessLogger(line => stdoutBuffer.append(line).append("\n"),
                                                  line => stderrBuffer.append(line).append("\n"))

  /**
    * Execute.
    *
    * @return a ProcessResult object
    */
  def apply[R <: ProcessResult](): ProcessResult = {
    // TODO: support stdin
    val exitCode = cmd ! processLogger
    val stdoutStr = stdoutBuffer.toString()
    val stderrStr = stderrBuffer.toString()

    stdoutBuffer.clear()
    stderrBuffer.clear()

    ProcessResult(exitCode, stdoutStr, stderrStr)
  }
}

/**
  * The return value of executing <code>CommandLineProcess</code>.  Contained within
  * this result is the process exit code, stdout, and stderr
  *
  * @param exitCode process exit code
  * @param stdout stdout as a String
  * @param stderr stderr as a String
  */
case class ProcessResult(exitCode: Int, stdout: String, stderr: String)

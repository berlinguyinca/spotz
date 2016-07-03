package com.eharmony.spotz.objective.vw

/**
  * @author vsuthichai
  */
object VwArgParser {
  def oneArg(args: String): String = {
    val spaceIndex = args.indexOf(" ")
    if (spaceIndex != -1)
      args.substring(0, spaceIndex)
    else
      args
  }

  def apply(args: String): Map[String, String] = {
    val arguments = args.trim.split("\\s+(?=-)")

    arguments.foldLeft(Map[String, String]()) { (argMap, argLine) =>
      val trimmedArgLine = argLine.trim
      val firstArg = oneArg(trimmedArgLine)

      val (argName, argValue) = if (firstArg.substring(0, 2) == "--") {
        (firstArg.substring(2), trimmedArgLine.substring(firstArg.length).trim)
      } else {
        (firstArg.substring(1, 2), trimmedArgLine.substring(2).trim)
      }

      argMap + (argName -> argValue)
    }
  }

  def apply(args: Option[String]): Map[String, String] = args.fold(Map[String, String]())(a => apply(a))

  def main(args: Array[String]) {
    val argMap = apply(args.mkString(" "))
    println(argMap)
  }
}

package com.eharmony.spotz.objective.vw

import com.eharmony.spotz.space.Point

import scala.collection.mutable

/**
  * @author vsuthichai
  */
trait VwFunctions {
  def mergeVwParams(vwParamMap: Map[String, String], point: Point) = {
    val vwParamsMutableMap = mutable.Map[String, Any]()

    vwParamMap.foldLeft(vwParamsMutableMap) { case (mutableMap, (k, v)) =>
      mutableMap += (k -> v)
    }

    point.getHyperParameterLabels.foldLeft(vwParamsMutableMap) { (mutableMap, vwHyperParam) =>
      mutableMap += (vwHyperParam -> point.get(vwHyperParam))
    }

    vwParamsMutableMap.remove("cache_file")
    vwParamsMutableMap.remove("f")
    vwParamsMutableMap.remove("t")
    vwParamsMutableMap.remove("i")

    vwParamsMutableMap
  }

  def vwParamMapToString(vwParamMap: Map[String, Any]): String = {
    vwParamMap.foldLeft(new StringBuilder) { case (sb, (vwArg, vwValue)) =>
      val dashes = if (vwArg.length == 1) "-" else "--"
      sb ++= s"$dashes$vwArg $vwValue "
    }.toString()
  }

  def getTrainVwParams(vwParamMap: Map[String, String], point: Point): String = {
    val vwParamsMutableMap = mergeVwParams(vwParamMap, point)
    vwParamMapToString(vwParamsMutableMap.toMap)
  }

  def getTestVwParams(vwParamMap: Map[String, String], point: Point): String = {
    vwParamMapToString(vwParamMap)
  }

  def parseVwArgs(args: Option[String]) = VwArgParser(args)
}

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

  def apply(args: Option[String]): Map[String, String] = args.fold(Map[String, String]())(apply)

  def main(args: Array[String]) {
    val argMap = apply(args.mkString(" "))
    println(argMap)
  }
}
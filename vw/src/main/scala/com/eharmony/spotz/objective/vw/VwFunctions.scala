package com.eharmony.spotz.objective.vw

import com.eharmony.spotz.Preamble.Point

import scala.collection.mutable

trait VwFunctions {

  /**
    * Merge hyperparameters from the point object into VW parameter map.  Hyperparameter
    * values will overwrite a map value during the merge if a hyerparameter label is the
    * same as a map key.
    *
    * Certain VW parameters are not allowed to be user specified because the objective
    * function needs control over them, ie. parameters related to cache, dataset,
    * testing, final regressor, etc.  These parameters are removed from the map so that
    * the objective function can specify them manually.
    *
    * @param vwParamMap a Map[String, String] where the key is a VW argument and the value
    *                   is the argument value.
    * @param point a point object representing the hyperparameter values
    * @return a new Map[String, String] which is the result of merging the vwParamMap and the point.
    */
  def mergeVwParams(vwParamMap: Map[String, _], point: Point): Map[String, _] = {
    val vwParamsMutableMap = mutable.Map[String, Any]()

    vwParamMap.foldLeft(vwParamsMutableMap) { case (mutableMap, (k, v)) =>
      mutableMap += ((k, v))
    }

    point.getHyperParameterLabels.foldLeft(vwParamsMutableMap) { (mutableMap, vwHyperParam) =>
      mutableMap += ((vwHyperParam, point.get(vwHyperParam)))
    }

    // Remove cache params
    vwParamsMutableMap.remove("cache_file")
    vwParamsMutableMap.remove("c")
    vwParamsMutableMap.remove("k")

    // Remove final regressor
    vwParamsMutableMap.remove("f")
    vwParamsMutableMap.remove("final_regressor")

    // Remove test mode
    vwParamsMutableMap.remove("t")

    // Remove input
    vwParamsMutableMap.remove("i")

    // Remove dataset param
    vwParamsMutableMap.remove("d")

    vwParamsMutableMap.toMap
  }

  def vwParamMapToString(vwParamMap: Map[String, _]): String = {
    vwParamMap.foldLeft(new StringBuilder) { case (sb, (vwArg, vwValue)) =>
      val dashes = if (vwArg.length == 1) "-" else "--"
      val vwParam = vwValue match {
        case value: String => s"$dashes$vwArg $vwValue "
        case value: Iterable[_] => value.map(x => s"$dashes$vwArg ${x.toString}").mkString(" ")
      }
      sb ++= vwParam
    }.toString()
  }

  def getTrainVwParams(vwParamMap: Map[String, _], point: Point): String = {
    vwParamMapToString(mergeVwParams(vwParamMap, point))
  }

  def getTestVwParams(vwParamMap: Map[String, _], point: Point): String = {
    vwParamMapToString(vwParamMap)
    // vwParamMapToString(mergeVwParams(vwParamMap, point))
  }

  def parseVwArgs(args: Option[String]) = VwArgParser(args)
}

/**
  * Parse VW parameter string and place the parameters in a Map where the key is the
  * parameter name and the Map value is the string value for that parameter.  If no
  * value follows the parameter name, then the Map value is an empty string.  If the
  * parameter name is specified with a value multiple times, then the Map value is an
  * Iterable of the multiple parameter values.
  *
  * This object is NOT infallible and can probably be broken, however it should work
  * for most if not nearly all the common VW parameter strings.  Once can choose to
  * put in parameter names that are not supported in VW.  No validation is done here to
  * ensure the parameter is actually supported, so this should be used carefully
  * with some common sense.  This object exists to provide convenience for VW parameter
  * parsing and manipulation.
  *
  * {{{
  *   scala> VwArgParser("-f vw.model -d dataset.txt")
  *   res0: Map[String,String] = Map(f -> vw.model, d -> dataset.txt)
  *
  *   scala> VwArgParser("--loss_function logistic -c -k --passes 100")
  *   res2: Map[String,String] = Map(loss_function -> logistic, c -> "", k -> "", passes -> 100)
  *
  *   scala> VwArgParser("-d train.dat --cb 4 -f cb.model  ")
  *   res3: Map[String,String] = Map(d -> train.dat, cb -> 4, f -> cb.model)
  *
  *   scala> VwArgParser("--cache_file cache_train --final_regressor r_temp --passes 3 --readable_model r_te--mp.txt --l1 lambda1")
  *   res4: Map[String,String] = Map(l1 -> lambda1, readable_model -> r_te--mp.txt, passes -> 3, cache_file -> cache_train, final_regressor -> r_temp)
  * }}}
  */
object VwArgParser {
  def oneArg(args: String): String = {
    val spaceIndex = args.indexOf(" ")
    if (spaceIndex != -1)
      args.substring(0, spaceIndex)
    else
      args
  }

  def apply(args: String): Map[String, Any] = {
    val arguments = args.trim.split("\\s+(?=-)")

    arguments.foldLeft(Map[String, Any]()) { (argMap, argLine) =>
      val trimmedArgLine = argLine.trim
      val firstArg = oneArg(trimmedArgLine)

      val (argName, argValue) = if (firstArg.substring(0, 2) == "--") {
        (firstArg.substring(2), trimmedArgLine.substring(firstArg.length).trim)
      } else {
        (firstArg.substring(1, 2), trimmedArgLine.substring(2).trim)
      }

      argMap + ((argName, argValue))
    }
  }

  def apply(args: Option[String]): Map[String, Any] = args.fold(Map[String, Any]())(apply)

  def main(args: Array[String]) {
    val argMap = apply(args.mkString(" "))
    println(argMap)
  }
}
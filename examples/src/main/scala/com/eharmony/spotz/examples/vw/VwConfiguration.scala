package com.eharmony.spotz.examples.vw

import com.eharmony.spotz.examples.Configuration

class VwCrossValidationConfiguration(args: Array[String]) extends Configuration(args) {
  val datasetPath = opt[String](name = "datasetPath", descr = "Absolute path to VW training dataset", required = true)
  val trainParams = opt[String](name = "trainParams", descr = "VW training parameters", required = true)
  val testParams = opt[String](name = "testParams", descr = "VW testing parameters", required = true)
  val numFolds = opt[Int](name = "folds", descr = "Number of folds", required = true)
}

class VwHoldoutConfiguration(args: Array[String]) extends Configuration(args) {
  val trainPath = opt[String](name = "trainPath", descr = "Absolute path to VW training dataset", required = true)
  val trainParams = opt[String](name = "trainParams", descr = "VW training parameters", required = true)
  val testPath = opt[String](name = "testPath", descr = "Absolute path to VW testing dataset", required = true)
  val testParams = opt[String](name = "testParams", descr = "VW testing parameters", required = true)
}

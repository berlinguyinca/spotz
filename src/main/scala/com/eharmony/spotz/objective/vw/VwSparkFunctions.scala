package com.eharmony.spotz.objective.vw

import java.io.PrintWriter

import com.eharmony.spotz.util.FileUtil
import org.apache.spark.SparkContext

/**
  * @author vsuthichai
  */
trait VwSparkFunctions {
  @transient val sc: SparkContext

  /**
    * Produce a VW cache file given a dataset in VW format and add it to the <code>SparkContext</code>.
    * This VW cache file is then accessible by Spark executors by obtaining the  absolute path of the
    * cache file through <code>SparkFiles</code>.
    *
    * @param vwDataset
    * @param vwCacheFilename
    */
  def addDatasetToSpark(vwDataset: Iterable[String], vwCacheFilename: String): String = {
    // Write VW dataset to a temporary file
    val vwDatasetFile = FileUtil.tempFile("dataset.vw")
    val vwDatasetWriter = new PrintWriter(vwDatasetFile)
    vwDataset.foreach(line => vwDatasetWriter.println(line))
    vwDatasetWriter.close()

    // Create a VW cache file from the dataset
    val vwCacheFile = FileUtil.tempFile(vwCacheFilename)
    val vwCacheProcess = VwProcess(s"-k --cache_file ${vwCacheFile.getAbsolutePath} -d ${vwDatasetFile.getAbsolutePath}")
    val vwCacheResult = vwCacheProcess()

    assert(vwCacheResult.exitCode == 0,
      s"VW Training cache exited with non-zero exit code ${vwCacheResult.exitCode}")

    sc.addFile(vwCacheFile.getAbsolutePath)
    vwCacheFile.getName
  }
}

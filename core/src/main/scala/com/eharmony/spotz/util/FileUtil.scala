package com.eharmony.spotz.util

import java.io.{File, InputStream}

import org.apache.commons.io.FilenameUtils
import org.apache.commons.vfs2.{FileNotFoundException, VFS}

import scala.io.Source

/**
  * @author vsuthichai
  */
object FileUtil {
  private val vfs2 = VFS.getManager

  /**
    * Return a file with a filename guaranteed not to be used on the file system.  This is
    * mainly used for files with a lifetime of a jvm run.
    *
    * @param prefix
    * @param suffix
    * @param deleteOnExit
    * @return
    */
  def tempFile(prefix: String, suffix: String, deleteOnExit: Boolean): File = {
    val f = File.createTempFile(s"$prefix-", s".$suffix")
    if (deleteOnExit)
      f.deleteOnExit()
    f
  }

  /**
    *
    * @param filename
    * @param deleteOnExit
    * @return
    */
  def tempFile(filename: String, deleteOnExit: Boolean = true): File = {
    tempFile(FilenameUtils.getBaseName(filename), FilenameUtils.getExtension(filename), deleteOnExit)
  }

  /**
    * Load the lines of a file as an iterator.
    *
    * @param path input path
    * @return lines of the file as an Iterator[String]
    */
  def loadFile(path: String): Iterator[String] = {
    Source.fromInputStream(loadFileInputStream(path)).getLines()
  }

  /**
    *
    * @param path
    * @return
    */
  def loadFileInputStream(path: String): InputStream = {
    val vfsFile = vfs2.resolveFile(path)
    vfsFile.getContent.getInputStream
  }
}

object SparkFileUtil {
  import org.apache.spark.SparkContext
  import org.apache.hadoop.mapred.InvalidInputException

  /**
    * Load the lines of a file as an iterator.  Also attempt to load the file from HDFS
    * since the SparkContext is available.
    *
    * @param path input path
    * @return lines of the file as an Iterator[String]
    */
  def loadFile(sc: SparkContext, path: String): Iterator[String] = {
    try {
      FileUtil.loadFile(path)
    } catch {
      case e: FileNotFoundException =>
        try {
          sc.textFile(path).toLocalIterator
        } catch {
          case e: InvalidInputException => Source.fromInputStream(this.getClass.getResourceAsStream(path)).getLines()
        }
    }
  }
}

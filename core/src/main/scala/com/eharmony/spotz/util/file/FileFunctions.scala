package com.eharmony.spotz.util.file

import java.io.{File, PrintWriter}

import com.eharmony.spotz.util.FileUtil
import org.apache.spark.{SparkContext, SparkFiles}

import scala.io.Source

/**
  * @author vsuthichai
  */
trait FileFunctions {
  def save(inputPath: String): String = save(Source.fromInputStream(FileUtil.loadFile(inputPath)).getLines())

  def save(inputIterable: Iterable[String]): String = save(inputIterable.toIterator)

  def save(inputIterator: Iterator[String]): String = {
    val tempFile = FileUtil.tempFile("file.temp")
    val printWriter = new PrintWriter(tempFile)
    inputIterator.foreach(line => printWriter.println(line))
    printWriter.close()
    save(tempFile)
  }

  def save(file: File): String
  def get(name: String): File
}

trait FileSystemFunctions extends FileFunctions {
  lazy val nameToAbsPath = scala.collection.mutable.Map[String, String]()

  override def save(file: File): String = {
    nameToAbsPath += ((file.getName, file.getAbsolutePath))
    file.getName
  }

  override def get(name: String): File = new File(nameToAbsPath(name))
}

trait SparkFileFunctions extends FileFunctions {
  val sc: SparkContext

  override def save(file: File): String = {
    sc.addFile(file.getAbsolutePath)
    file.getName
  }

  override def get(name: String): File = new File(SparkFiles.get(name))
}
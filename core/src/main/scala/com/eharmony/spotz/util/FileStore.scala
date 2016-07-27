package com.eharmony.spotz.util

import java.io.{File, PrintWriter}

import org.apache.spark.{SparkContext, SparkFiles}

import scala.io.Source

/**
  * The file store is an abstraction that manages extraneous files that need to
  * be used by various objective functions.  This needs to be flexible enough
  * to work within a Spark environment and outside a Spark environment.  This
  * implementation heavily uses the cake pattern.
  *
  * @author vsuthichai
  */
trait FileStoreFunctions {
  def fileStoreWriter: FileStoreWriter
  def fileStoreReader: FileStoreReader

  class FileStoreWriter {
    def add(inputPath: String): File = {
      val lines = Source.fromInputStream(FileUtil.loadFile(inputPath)).getLines()
      add(lines)
    }

    def add(inputIterable: Iterable[String]): File = add(inputIterable.toIterator)

    def add(inputIterator: Iterator[String]): File = {
      val tempFile = FileUtil.tempFile("file.temp")
      val printWriter = new PrintWriter(tempFile)
      inputIterator.foreach(line => printWriter.println(line))
      printWriter.close()

      tempFile
    }
  }

  trait FileStoreReader {
    def getFile(filename: String): File
  }
}

trait SparkFileStoreFunctions extends FileStoreFunctions {
  val sparkContext: SparkContext

  def fileStoreWriter = new SparkFileUploader
  def fileStoreReader = new SparkFileReader

  class SparkFileUploader extends FileStoreWriter {
    def saveFile(lines: Iterator[String]): String = {
      val file = add(lines)
      sparkContext.addFile(file.getAbsolutePath)
      file.getName
    }
  }

  class SparkFileReader extends FileStoreReader {
    override def getFile(vwCacheFilename: String): File = {
      new File(SparkFiles.get(vwCacheFilename))
    }
  }
}

trait FileSystemFunctions extends FileStoreFunctions {
  private[this] val filenameToAbsPath = Map[String, String]()

  def fileStoreWriter = new FileSystemWriter
  def fileStoreReader = new FileSystemReader

  class FileSystemWriter extends FileStoreWriter {
    def saveFile(lines: Iterator[String]): String = {
      val file = add(lines)
      filenameToAbsPath + ((file.getName, file.getAbsolutePath))
      file.getName
    }
  }

  class FileSystemReader extends FileStoreReader {
    override def getFile(filename: String): File = {
      new File(filenameToAbsPath(filename))
    }
  }
}

trait FileStore {
  def saveFile(lines: Iterator[String]): String
  def getFile(filename: String): File
}

trait SparkFileStore extends FileStore { this: SparkFileStoreFunctions =>
  def saveFile(lines: Iterator[String]): String = fileStoreWriter.saveFile(lines)
  def getFile(filename: String): File = fileStoreReader.getFile(filename)
}

trait FileSystemStore extends FileStore { this: FileSystemFunctions =>
  def saveFile(lines: Iterator[String]): String = fileStoreWriter.saveFile(lines)
  def getFile(filename: String): File = fileStoreReader.getFile(filename)
}

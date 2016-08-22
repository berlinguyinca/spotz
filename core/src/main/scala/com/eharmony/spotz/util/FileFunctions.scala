package com.eharmony.spotz.util

import java.io.{File, PrintWriter}

import org.apache.spark.{SparkContext, SparkFiles}

/**
  * Provide capability to save and retrieve files from inside the objective
  * functions.  Users are free to interact with the underlying file system freely as they desire,
  * but this trait provides a layer to simplify certain details of those file interactions with the
  * chosen backend computation engine.  The <code>save</code> methods are intended to be used
  * within an objective function's constructor code and not within its <code>apply</code> method.
  * Later when the objection function is being parallelized, the file can be retrieved with the
  * <code>get</method> inside the <code>apply</code> method.
  */
trait FileFunctions {
  def save(inputPath: String): String = save(FileUtil.loadFile(inputPath))

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

/**
  * This trait is intended for handling files when parallel collections are used to do the computation.
  * It interacts directly with the file system since parallel collections run on a single node.  Calling
  * <code>save</code> on a file will return a key that can later be used to retrieve that same file
  * later inside the <code>apply</code> method of the objective function.
  */
trait FileSystemFunctions extends FileFunctions {
  lazy val nameToAbsPath = scala.collection.mutable.Map[String, String]()

  override def save(file: File): String = {
    nameToAbsPath += ((file.getName, file.getAbsolutePath))
    file.getName
  }

  override def get(name: String): File = new File(nameToAbsPath(name))
}

/**
  * The functions of this trait are responsible for adding a file to a SparkContext and
  * retrieving that same file on a worker node.  Adding a file to the SparkContext must be done
  * on the driver, while retrieving the file is done on a worker.  Consequently, when an objective
  * function executes on a worker and tries to access the file, the file must have already been added
  * to the SparkContext before the objective function gets executed.  This provides a few options.
  *
  * 1) The user can manually add the file themselves to the SparkContext and then retrieve it later
  * from the SparkContext while the objective function executes on the worker.
  *
  * or
  *
  * 2) The user can mix this trait in to the objective function and call this trait's <code>save</code>
  * method from within the objective's constructor, ie. the class code body.  The saved file can then be
  * accessed from the <code>apply</code> method of the objective function as it's executing on the worker
  * through this same trait's <code>get</code> method.
  */
trait SparkFileFunctions extends FileFunctions {
  val sc: SparkContext

  override def save(file: File): String = {
    sc.addFile(file.getAbsolutePath)
    file.getName
  }

  override def get(name: String): File = new File(SparkFiles.get(name))
}

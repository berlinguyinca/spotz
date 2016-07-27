package com.eharmony.spotz.util

import java.io.{File, InputStream}

import org.apache.commons.io.FilenameUtils
import org.apache.commons.vfs2.{FileSystemException, VFS}

/**
  * @author vsuthichai
  */
object FileUtil {
  val vfs2 = VFS.getManager

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

  def tempFile(filename: String, deleteOnExit: Boolean = true): File = {
    tempFile(FilenameUtils.getBaseName(filename), FilenameUtils.getExtension(filename), deleteOnExit)
  }

  def loadFile(path: String): InputStream = {
    val vfsFile = vfs2.resolveFile(path)
    vfsFile.getContent.getInputStream
  }
}

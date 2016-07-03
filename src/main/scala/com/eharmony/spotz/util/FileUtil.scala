package com.eharmony.spotz.util

import java.io.File
import java.io.InputStream

import org.apache.commons.vfs2.VFS

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
  def tempFile(prefix: String, suffix: String, deleteOnExit: Boolean = true): File = {
    val f = File.createTempFile(prefix, suffix)
    if (deleteOnExit)
      f.deleteOnExit()
    f
  }

  def loadFile(path: String): InputStream = {
    val file = vfs2.resolveFile(path)
    file.getContent.getInputStream
  }
}

package com.eharmony.spotz.util

import java.io.File

/**
 * @author vsuthichai
 */
object FileUtil {

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
}

package com.jakewharton.diffuse

import com.jakewharton.diffuse.ArchiveFile.Type.Companion.toJarFileType
import com.jakewharton.diffuse.ArchiveFiles.Companion.toArchiveFiles
import com.jakewharton.diffuse.io.Input

class Jar(
  override val filename: String?,
  val files: ArchiveFiles
) : Binary {
  companion object {
    @JvmStatic
    @JvmName("parse")
    fun Input.toJar(): Jar {
      toZip().use { zip ->
        val files = zip.toArchiveFiles { it.toJarFileType() }
        return Jar(name, files)
      }
    }
  }
}

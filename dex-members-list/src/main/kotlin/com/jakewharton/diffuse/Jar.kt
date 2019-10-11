package com.jakewharton.diffuse

import com.jakewharton.dex.readBytes
import com.jakewharton.diffuse.ArchiveFile.Type.Companion.toJarFileType
import com.jakewharton.diffuse.ArchiveFiles.Companion.toArchiveFiles
import okio.ByteString
import okio.ByteString.Companion.toByteString
import java.nio.file.Path

class Jar(
  override val filename: String?,
  val files: ArchiveFiles
) : Binary {
  companion object {
    @JvmStatic
    @JvmName("parse")
    fun ByteString.toJar(name: String): Jar {
      val files = toArchiveFiles { it.toJarFileType() }
      return Jar(name, files)
    }

    @JvmStatic
    @JvmName("parse")
    fun Path.toJar(): Jar = readBytes().toByteString().toJar(fileName.toString())
  }
}

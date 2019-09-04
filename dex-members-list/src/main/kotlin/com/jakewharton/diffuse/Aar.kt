package com.jakewharton.diffuse

import com.jakewharton.dex.readBytes
import com.jakewharton.diffuse.ArchiveFile.Type.Companion.toAarFileType
import okio.ByteString
import okio.ByteString.Companion.toByteString
import java.nio.file.Path
import java.util.SortedMap

class Aar private constructor(
  override val filename: String?,
  override val bytes: ByteString
) : Binary {
  val files: SortedMap<String, ArchiveFile> by lazy {
    bytes.parseArchiveFiles { it.toAarFileType() }
  }

  // TODO val classes.jar = TODO()
  // TODO val api.jar = TODO()

  companion object {
    @JvmStatic
    @JvmName("create")
    fun Path.toAar() = Aar(fileName.toString(), readBytes().toByteString())
  }
}

package com.jakewharton.diffuse

import com.jakewharton.dex.entries
import com.jakewharton.dex.readBytes
import com.jakewharton.diffuse.ArchiveFile.Type.Companion.toApkFileType
import okio.Buffer
import okio.ByteString
import okio.ByteString.Companion.toByteString
import java.nio.file.Path
import java.util.SortedMap
import java.util.zip.ZipInputStream

class Aar private constructor(
  override val filename: String?,
  override val bytes: ByteString
) : Binary {
  val files: SortedMap<String, ArchiveFile> by lazy {
    ZipInputStream(Buffer().write(bytes).inputStream()).use { zis ->
      zis.entries()
          .associate { entry ->
            entry.name to ArchiveFile(entry.name, entry.name.toApkFileType(),
                Size(entry.compressedSize),
                Size(entry.size))
          }
          .toSortedMap()
    }
  }
  // TODO val classes.jar = TODO()
  // TODO val api.jar = TODO()

  companion object {
    @JvmStatic
    @JvmName("create")
    fun Path.toAar() = Aar(fileName.toString(), readBytes().toByteString())
  }
}

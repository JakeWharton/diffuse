package com.jakewharton.diffuse

import com.jakewharton.dex.entries
import com.jakewharton.dex.readBytes
import com.jakewharton.diffuse.ArchiveFile.Type.Companion.toApkFileType
import com.jakewharton.diffuse.Arsc.Companion.toArsc
import com.jakewharton.diffuse.Dex.Companion.toDex
import okio.Buffer
import okio.ByteString
import okio.ByteString.Companion.toByteString
import java.nio.file.Path
import java.util.zip.ZipInputStream

class Apk private constructor(
  val filename: String?,
  val bytes: ByteString
) {
  val files: List<ArchiveFile> by lazy {
    ZipInputStream(Buffer().write(bytes).inputStream()).use { zis ->
      zis.entries()
          .map { entry ->
            ArchiveFile(entry.name, entry.name.toApkFileType(),
                Size(entry.compressedSize),
                Size(entry.size))
          }
          .toList()
    }
  }
  val dexes: List<Dex> by lazy {
    ZipInputStream(Buffer().write(bytes).inputStream()).use { zis ->
      zis.entries()
          .filter { it.name.endsWith(".dex") }
          .map { zis.readBytes().toByteString().toDex() }
          .toList()
    }
  }
  val arsc: Arsc by lazy {
    ZipInputStream(Buffer().write(bytes).inputStream()).use { zis ->
      zis.entries().first { it.name.endsWith(".arsc") }
      zis.readBytes().toByteString().toArsc()
    }
  }

  companion object {
    @JvmStatic
    @JvmName("create")
    fun Path.toApk() = Apk(fileName.toString(), readBytes().toByteString())
  }
}

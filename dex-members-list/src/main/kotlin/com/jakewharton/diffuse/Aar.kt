package com.jakewharton.diffuse

import com.jakewharton.dex.entries
import com.jakewharton.dex.readBytes
import com.jakewharton.diffuse.AndroidManifest.Companion.toAndroidManifest
import com.jakewharton.diffuse.ArchiveFile.Type.Companion.toAarFileType
import okio.Buffer
import okio.ByteString
import okio.ByteString.Companion.toByteString
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.Path
import java.util.SortedMap
import java.util.zip.ZipInputStream

class Aar private constructor(
  override val filename: String?,
  override val bytes: ByteString
) : Binary {
  val files: SortedMap<String, ArchiveFile> by lazy {
    bytes.parseArchiveFiles { it.toAarFileType() }
  }

  // TODO val classes.jar = TODO()
  // TODO val api.jar = TODO()

  val manifest: AndroidManifest by lazy {
    ZipInputStream(Buffer().write(bytes).inputStream()).use { zis ->
      zis.entries().first { it.name == "AndroidManifest.xml" }
      zis.readBytes().toString(UTF_8).toAndroidManifest()
    }
  }

  companion object {
    @JvmStatic
    @JvmName("create")
    fun Path.toAar() = Aar(fileName.toString(), readBytes().toByteString())
  }
}

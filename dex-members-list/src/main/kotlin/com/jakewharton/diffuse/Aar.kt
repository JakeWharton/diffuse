package com.jakewharton.diffuse

import com.jakewharton.dex.entries
import com.jakewharton.dex.readBytes
import com.jakewharton.diffuse.AndroidManifest.Companion.toAndroidManifest
import com.jakewharton.diffuse.ArchiveFile.Type.Companion.toAarFileType
import com.jakewharton.diffuse.ArchiveFiles.Companion.toArchiveFiles
import okio.Buffer
import okio.ByteString.Companion.toByteString
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.Path
import java.util.zip.ZipInputStream

class Aar private constructor(
  override val filename: String?,
  val files: ArchiveFiles,
  val manifest: AndroidManifest
) : Binary {

  // TODO val classes.jar = TODO()
  // TODO val api.jar = TODO()

  companion object {
    @JvmStatic
    @JvmName("create")
    fun Path.toAar(): Aar {
      val bytes = readBytes().toByteString()
      val files = bytes.toArchiveFiles { it.toAarFileType() }
      val manifest: AndroidManifest by lazy {
        ZipInputStream(Buffer().write(bytes).inputStream()).use { zis ->
          zis.entries().first { it.name == "AndroidManifest.xml" }
          zis.readBytes().toString(UTF_8).toAndroidManifest()
        }
      }
      return Aar(fileName.toString(), files, manifest)
    }
  }
}

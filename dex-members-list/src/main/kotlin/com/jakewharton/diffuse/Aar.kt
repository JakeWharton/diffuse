package com.jakewharton.diffuse

import com.jakewharton.dex.entries
import com.jakewharton.dex.readBytes
import com.jakewharton.diffuse.AndroidManifest.Companion.toAndroidManifest
import com.jakewharton.diffuse.ArchiveFile.Type.Companion.toAarFileType
import com.jakewharton.diffuse.ArchiveFiles.Companion.toArchiveFiles
import com.jakewharton.diffuse.Jar.Companion.toJar
import okio.ByteString.Companion.toByteString
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.Path

class Aar private constructor(
  override val filename: String?,
  val files: ArchiveFiles,
  val manifest: AndroidManifest,
  val classes: Jar,
  val libs: List<Jar>
) : Binary {
  companion object {
    @JvmStatic
    @JvmName("create")
    fun Path.toAar(): Aar {
      val bytes = readBytes().toByteString()
      val files = bytes.toArchiveFiles { it.toAarFileType() }
      val manifest = bytes.asInputStream().asZip().use { zis ->
        zis.entries().first { it.name == "AndroidManifest.xml" }
        zis.readBytes().toString(UTF_8).toAndroidManifest()
      }
      val classes = bytes.asInputStream().asZip().use { zis ->
        zis.entries().first { it.name == "classes.jar" }
        zis.readByteString().toJar("classes.jar")
      }
      val libs = bytes.asInputStream().asZip().use { zis ->
        zis.entries()
            .filter { it.name.startsWith("libs/") && it.name.endsWith(".jar") }
            .map { zis.readByteString().toJar(it.name) }
            .toList()
      }
      return Aar(fileName.toString(), files, manifest, classes, libs)
    }
  }
}

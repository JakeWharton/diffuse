package com.jakewharton.diffuse.format

import com.jakewharton.diffuse.format.AndroidManifest.Companion.toManifest
import com.jakewharton.diffuse.format.ArchiveFile.Type.Companion.toAarFileType
import com.jakewharton.diffuse.format.ArchiveFiles.Companion.toArchiveFiles
import com.jakewharton.diffuse.format.Jar.Companion.toJar
import com.jakewharton.diffuse.io.Input

class Aar private constructor(
  override val filename: String?,
  val files: ArchiveFiles,
  val manifest: AndroidManifest,
  val classes: Jar,
  val libs: List<Jar>,
) : BinaryFormat {
  /** The `classes.jar` and any additional jars from `libs/`. */
  // TODO remove toTypedArray call https://youtrack.jetbrains.com/issue/KT-12663
  val jars get() = listOf(classes, *libs.toTypedArray())

  companion object {
    internal val libsJarRegex = Regex("libs/[^/]\\.jar")

    @JvmStatic
    @JvmName("parse")
    fun Input.toAar(): Aar {
      toZip().use { zip ->
        val files = zip.toArchiveFiles { it.toAarFileType() }
        val manifest = zip[AndroidManifest.NAME].asInput().toManifest()
        val classes = zip["classes.jar"].asInput().toJar()
        val libs = zip.entries
          .filter { it.path.matches(libsJarRegex) }
          .map { it.asInput().toJar() }
        return Aar(name, files, manifest, classes, libs)
      }
    }
  }
}

package com.jakewharton.diffuse

import com.jakewharton.diffuse.ArchiveFile.Type.Companion.toAarFileType
import com.jakewharton.diffuse.ArchiveFiles.Companion.toArchiveFiles
import com.jakewharton.diffuse.Jar.Companion.toJar
import com.jakewharton.diffuse.Manifest.Companion.toManifest
import com.jakewharton.diffuse.io.Input

class Aar private constructor(
  override val filename: String?,
  val files: ArchiveFiles,
  val manifest: Manifest,
  val classes: Jar,
  val libs: List<Jar>
) : Binary {
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
        val manifest = zip[Apk.manifestFileName].asInput().toUtf8().toManifest()
        val classes = zip["classes.jar"].asInput().toJar()
        val libs = zip.entries
            .filter { it.path.matches(libsJarRegex) }
            .map { it.asInput().toJar() }
        return Aar(name, files, manifest, classes, libs)
      }
    }
  }
}

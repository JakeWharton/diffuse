package com.jakewharton.diffuse.format

import com.jakewharton.diffuse.format.ArchiveFile.Type.Companion.toJarFileType
import com.jakewharton.diffuse.format.ArchiveFiles.Companion.toArchiveFiles
import com.jakewharton.diffuse.format.Class.Companion.toClass
import com.jakewharton.diffuse.io.Input

class Jar
private constructor(
  override val filename: String?,
  val manifest: String,
  val files: ArchiveFiles,
  val classes: List<Class>,
  override val declaredMembers: List<Member>,
  override val referencedMembers: List<Member>,
) : BinaryFormat, CodeBinary {
  override val members = declaredMembers + referencedMembers

  companion object {
    private const val MANIFEST_PATH = "META-INF/MANIFEST.MF"

    @JvmStatic
    @JvmName("parse")
    fun Input.toJar(): Jar {
      toZip().use { zip ->
        val files = zip.toArchiveFiles { it.toJarFileType() }

        val classes =
          zip.entries.filter { it.path.endsWith(".class") }.map { it.asInput().toClass() }

        val declaredMembers = classes.flatMapTo(LinkedHashSet()) { it.declaredMembers }
        val referencedMembers = classes.flatMapTo(LinkedHashSet()) { it.referencedMembers }
        // Declared methods are likely to reference other declared members. Ensure all are removed.
        referencedMembers -= declaredMembers

        return Jar(
          filename = name,
          manifest = zip[MANIFEST_PATH].asInput().toUtf8(),
          files = files,
          classes = classes,
          declaredMembers = declaredMembers.sorted(),
          referencedMembers = referencedMembers.sorted(),
        )
      }
    }
  }
}

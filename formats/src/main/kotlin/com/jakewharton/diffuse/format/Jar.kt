package com.jakewharton.diffuse.format

import com.jakewharton.diffuse.format.ArchiveFile.Type.Companion.toJarFileType
import com.jakewharton.diffuse.format.ArchiveFiles.Companion.toArchiveFiles
import com.jakewharton.diffuse.format.Class.Companion.toClass
import com.jakewharton.diffuse.io.Input

class Jar private constructor(
  override val filename: String?,
  val files: ArchiveFiles,
  val classes: List<Class>,
  override val declaredMembers: List<Member>,
  override val referencedMembers: List<Member>,
) : BinaryFormat, CodeBinary {
  override val members = declaredMembers + referencedMembers

  companion object {
    @JvmStatic
    @JvmName("parse")
    fun Input.toJar(): Jar {
      toZip().use { zip ->
        val files = zip.toArchiveFiles { it.toJarFileType() }

        val classes = zip.entries
          .filter { it.path.endsWith(".class") }
          .map { it.asInput().toClass() }

        val declaredMembers = classes.flatMap { it.declaredMembers }
        val referencedMembers = classes.flatMapTo(LinkedHashSet()) { it.referencedMembers }
        // Declared methods are likely to reference other declared members. Ensure all are removed.
        referencedMembers -= declaredMembers

        return Jar(name, files, classes, declaredMembers.sorted(), referencedMembers.sorted())
      }
    }
  }
}

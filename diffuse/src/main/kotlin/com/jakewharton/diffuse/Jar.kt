package com.jakewharton.diffuse

import com.jakewharton.diffuse.ArchiveFile.Type.Companion.toJarFileType
import com.jakewharton.diffuse.ArchiveFiles.Companion.toArchiveFiles
import com.jakewharton.diffuse.Class.Companion.toClass
import com.jakewharton.diffuse.io.Input

class Jar private constructor(
  override val filename: String?,
  val files: ArchiveFiles,
  val classes: List<Class>,
  val declaredMembers: List<Member>,
  val referencedMembers: List<Member>
) : Binary {
  val members = declaredMembers + referencedMembers

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

package com.jakewharton.diffuse

import com.jakewharton.diffuse.ArchiveFile.Type.Companion.toJarFileType
import com.jakewharton.diffuse.ArchiveFiles.Companion.toArchiveFiles
import com.jakewharton.diffuse.Class.Companion.toClass
import com.jakewharton.diffuse.io.Input

class Jar private constructor(
  override val filename: String?,
  val files: ArchiveFiles,
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

        val declaredMembers = mutableListOf<Member>()
        val referencedMembers = mutableSetOf<Member>()
        zip.entries
            .filter { it.path.endsWith(".class") }
            .forEach { entry ->
              val cls = entry.asInput().toClass()
              declaredMembers += cls.declaredMembers
              referencedMembers += cls.referencedMembers
            }

        // Declared methods are likely to reference other declared members.
        referencedMembers -= declaredMembers

        return Jar(name, files, declaredMembers.sorted(), referencedMembers.sorted())
      }
    }
  }
}

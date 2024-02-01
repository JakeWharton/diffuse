package com.jakewharton.diffuse.format

import com.jakewharton.diffuse.format.ArchiveFile.Type.Companion.toJarFileType
import com.jakewharton.diffuse.format.ArchiveFiles.Companion.toArchiveFiles
import com.jakewharton.diffuse.format.Class.Companion.toClass
import com.jakewharton.diffuse.io.Input

class Jar private constructor(
  override val filename: String?,
  val bytecodeVersion: Short?,
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
        val bcVersions = mutableMapOf<Short, Long>()

        val classes = zip.entries
          .asSequence()
          .filter { it.path.endsWith(".class") }
          .map { entry ->
            entry.asInput().toClass().also { cls ->
              // Count all the byte code versions in a Jar.
              bcVersions.merge(cls.bytecodeVersion, 1L, Long::plus)
            }
          }
          .toList()

        val mostBytecodeVersion = bcVersions.maxByOrNull { it.value }?.key

        val declaredMembers = classes.flatMap { it.declaredMembers }
        val referencedMembers = classes.flatMapTo(LinkedHashSet()) { it.referencedMembers }
        // Declared methods are likely to reference other declared members. Ensure all are removed.
        referencedMembers -= declaredMembers

        return Jar(name, mostBytecodeVersion, files, classes, declaredMembers.sorted(), referencedMembers.sorted())
      }
    }
  }
}

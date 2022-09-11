package com.jakewharton.diffuse.format

import com.jakewharton.diffuse.format.ArchiveFile.Type
import com.jakewharton.diffuse.format.ArchiveFile.Type.Other
import com.jakewharton.diffuse.io.Size
import com.jakewharton.diffuse.io.Zip

class ArchiveFiles internal constructor(
  private val files: Map<String, ArchiveFile>,
) : Map<String, ArchiveFile> by files {
  override fun hashCode() = files.hashCode()
  override fun equals(other: Any?) = other is ArchiveFiles && files == other.files

  companion object {
    @JvmStatic
    @JvmName("parse")
    fun Zip.toArchiveFiles(classifier: (String) -> Type): ArchiveFiles {
      return entries
        .associate {
          it.path to ArchiveFile(it.path, classifier(it.path), it.zipSize, it.uncompressedSize, it.isCompressed)
        }
        .plus("/" to ArchiveFile("/", Other, Size(22), Size.ZERO, false))
        .let(::ArchiveFiles)
    }
  }
}

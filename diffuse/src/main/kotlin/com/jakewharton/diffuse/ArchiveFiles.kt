package com.jakewharton.diffuse

import com.jakewharton.diffuse.ArchiveFile.Type
import com.jakewharton.diffuse.ArchiveFile.Type.Other
import com.jakewharton.diffuse.io.Zip

class ArchiveFiles internal constructor(
  private val files: Map<String, ArchiveFile>
) : Map<String, ArchiveFile> by files {
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

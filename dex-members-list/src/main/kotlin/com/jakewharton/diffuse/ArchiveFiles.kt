package com.jakewharton.diffuse

import com.jakewharton.dex.entries
import com.jakewharton.diffuse.ArchiveFile.Type
import com.jakewharton.diffuse.ArchiveFile.Type.Other
import okio.ByteString
import okio.utf8Size

class ArchiveFiles internal constructor(
  private val files: Map<String, ArchiveFile>
) : Map<String, ArchiveFile> by files {
  companion object {
    @JvmStatic
    @JvmName("parse")
    fun ByteString.toArchiveFiles(classifier: (String) -> Type) : ArchiveFiles {
      return asInputStream().asZip().use { zis ->
        zis.entries()
            .associate { entry ->
              val nameSize = entry.name.utf8Size()
              val extraSize = entry.extra?.size ?: 0
              val commentSize = entry.comment?.utf8Size() ?: 0

              // Calculate the actual compressed size impact in the zip, not just compressed data size.
              // See https://en.wikipedia.org/wiki/Zip_(file_format)#File_headers for details.
              val compressedSize = entry.compressedSize +
                  // Local file header. There is no way of knowing whether a trailing data descriptor
                  // was present since the general flags field is not exposed, but it's unlikely.
                  30 + nameSize + extraSize +
                  // Central directory file header.
                  46 + nameSize + extraSize + commentSize

              entry.name to ArchiveFile(entry.name, classifier(entry.name), Size(compressedSize),
                  Size(entry.size)
              )
            }
            // Include a dummy root entry to account for the end of central directory record. There is
            // no way of accessing the zip comment using ZipInputStream, but it's likely empty.
            .plus("/" to ArchiveFile("/", Other, Size(22), Size.ZERO))
            .let(::ArchiveFiles)
      }
    }
  }
}

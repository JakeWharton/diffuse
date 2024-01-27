package com.jakewharton.diffuse.io

import com.jakewharton.diffuse.io.Input.Companion.asInput
import java.io.Closeable
import java.io.FileNotFoundException
import java.nio.file.FileSystem
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import okio.ByteString
import okio.utf8Size

interface Zip : Closeable {
  val entries: List<Entry>

  fun directoryView(name: String): Zip {
    val parent = this
    val prefix = "$name/"
    return object : Zip {
      override val entries = parent.entries.mapNotNull {
        if (it.path.startsWith(prefix)) {
          object : Entry by it {
            override val path: String
              get() = it.path.substring(prefix.length)
          }
        } else {
          null
        }
      }

      override fun get(path: String) = parent[prefix + path]

      override fun close() {
        // No-op. Hopefully someone is treating the enclosing Zip as a resource.
      }
    }
  }

  fun find(path: String) = entries.firstOrNull { it.path == path }

  operator fun get(path: String) =
    find(path) ?: throw FileNotFoundException("No entry: $path")

  interface Entry {
    val path: String

    /** The uncompressed size of the entry contents */
    val uncompressedSize: Size

    /** The compressed size of the entry contents. */
    val compressedSize: Size

    /** The impact on the overall zip size. This is [compressedSize] plus metadata. */
    val zipSize: Size

    /** Returns true if the entry is compressed using any method other than 'store'. */
    val isCompressed: Boolean

    fun asInput(): Input
  }
}

private fun <T : Zip.Entry> ZipInputStream.mapEntries(
  entryFactory: (name: String, size: Size, compressedSize: Size, zipSize: Size, isCompressed: Boolean) -> T,
): List<T> {
  return entries().map { it.toZipEntry(this, entryFactory) }.toList()
}

private fun <T : Zip.Entry> ZipEntry.toZipEntry(
  zipStream: ZipInputStream,
  entryFactory: (name: String, size: Size, compressedSize: Size, zipSize: Size, isCompressed: Boolean) -> T,
): T {
  val isCompressed = method != ZipEntry.STORED
  val nameSize = name.utf8Size()
  val extraSize = extra?.size ?: 0
  val commentSize = comment?.utf8Size() ?: 0

  var dataDescriptorSize = 0
  if (compressedSize == -1L || size == -1L) {
    // Entries which were streamed have their compressed and original size stored in a data descriptor
    // record which follows the entry data. The values will only be populated when the data has been
    // fully read. This call is free as reading the next entry will close this entry anyway.
    zipStream.closeEntry()

    // We assume the data descriptor includes the optional-but-recommended 4-byte signature prefix.
    dataDescriptorSize = 16

    if (size > UInt.MAX_VALUE.toLong()) {
      // Assuming the original size is always larger than the compressed size, if it exceeds the
      // amount that can fit into a 4-byte integer we're a zip64. Note that zip64 _could_ still have
      // been used with small files, but it _must_ have been used with ones this large.
      dataDescriptorSize += 8
    }
  }

  // Calculate the actual compressed size impact in the zip, not just compressed data size.
  // See https://en.wikipedia.org/wiki/Zip_(file_format)#File_headers for details.
  val zipSize = compressedSize +
    // Local file header.
    30 + nameSize + extraSize + dataDescriptorSize +
    // Central directory file header.
    46 + nameSize + extraSize + commentSize

  return entryFactory(name, Size(size), Size(compressedSize), Size(zipSize), isCompressed)
}

internal fun Path.toZip(): Zip {
  val fs = asZipFileSystem()
  val root = fs.rootDirectories.single()!!
  val entries = inputStream().use {
    it.asZip().mapEntries { name, size, compressedSize, zipSize, isCompressed ->
      PathZip.Entry(root, name, size, compressedSize, zipSize, isCompressed)
    }
  }
  return PathZip(fs, entries)
}

internal class PathZip(
  fs: FileSystem,
  override val entries: List<Zip.Entry>,
) : Zip, Closeable by fs {
  class Entry(
    private val root: Path,
    override val path: String,
    override val uncompressedSize: Size,
    override val compressedSize: Size,
    override val zipSize: Size,
    override val isCompressed: Boolean,
  ) : Zip.Entry {
    override fun asInput(): Input {
      val child = root.resolve(path)
      if (!child.exists()) {
        throw FileNotFoundException("No entry: $path")
      }
      return child.asInput()
    }
  }
}

internal fun ByteString.toZip(): Zip {
  val entries = asInputStream().asZip().mapEntries { name, size, compressedSize, zipSize, isCompressed ->
    BytesZip.Entry(this@toZip, name, size, compressedSize, zipSize, isCompressed)
  }
  return BytesZip(entries)
}

internal class BytesZip(
  override val entries: List<Zip.Entry>,
) : Zip {
  class Entry(
    private val bytes: ByteString,
    override val path: String,
    override val uncompressedSize: Size,
    override val compressedSize: Size,
    override val zipSize: Size,
    override val isCompressed: Boolean,
  ) : Zip.Entry {
    override fun asInput(): Input {
      val zis = bytes.asInputStream().asZip()
      while (true) {
        val entry = zis.nextEntry ?: break
        if (entry.name == path) {
          return zis.readByteString().asInput(entry.name)
        }
      }
      throw FileNotFoundException("No entry: $path")
    }
  }

  override fun close() = Unit
}

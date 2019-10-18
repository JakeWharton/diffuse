package com.jakewharton.diffuse.io

import com.jakewharton.diffuse.Size
import com.jakewharton.diffuse.asInputStream
import com.jakewharton.diffuse.asZip
import com.jakewharton.diffuse.asZipFileSystem
import com.jakewharton.diffuse.entries
import com.jakewharton.diffuse.exists
import com.jakewharton.diffuse.inputStream
import com.jakewharton.diffuse.io.Input.Companion.asInput
import com.jakewharton.diffuse.readByteString
import okio.ByteString
import okio.utf8Size
import java.io.Closeable
import java.io.FileNotFoundException
import java.nio.file.FileSystem
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

interface Zip : Closeable {
  val entries: List<Entry>

  operator fun get(path: String) =
    entries.firstOrNull { it.path == path }
        ?: throw FileNotFoundException("No entry: $path")

  interface Entry {
    val path: String
    /** The uncompressed size of the entry contents */
    val uncompressedSize: Size
    /** The compressed size of the entry contents. */
    val compressedSize: Size
    /** The impact on the overall zip size. This is [compressedSize] plus metadata. */
    val zipSize: Size

    fun asInput(): Input
  }
}

private fun <T : Zip.Entry> ZipInputStream.mapEntries(
  entryFactory: (name: String, size: Size, compressedSize: Size, zipSize: Size) -> T
): List<T> {
  return entries().map { it.toZipEntry(entryFactory) }.toList()
}

private fun <T : Zip.Entry> ZipEntry.toZipEntry(
  entryFactory: (name: String, size: Size, compressedSize: Size, zipSize: Size) -> T
): T {
  val nameSize = name.utf8Size()
  val extraSize = extra?.size ?: 0
  val commentSize = comment?.utf8Size() ?: 0
  // Calculate the actual compressed size impact in the zip, not just compressed data size.
  // See https://en.wikipedia.org/wiki/Zip_(file_format)#File_headers for details.
  val zipSize = compressedSize +
      // Local file header. There is no way of knowing whether a trailing data descriptor
      // was present since the general flags field is not exposed, but it's unlikely.
      30 + nameSize + extraSize +
      // Central directory file header.
      46 + nameSize + extraSize + commentSize

  return entryFactory(name, Size(size), Size(compressedSize), Size(zipSize))
}

internal fun Path.toZip(): Zip {
  val fs = asZipFileSystem()
  val root = fs.rootDirectories.single()!!
  val entries = inputStream().use {
    it.asZip().mapEntries { name, size, compressedSize, zipSize ->
      PathZip.Entry(root, name, size, compressedSize, zipSize)
    }
  }
  return PathZip(fs, entries)
}

internal class PathZip(
  fs: FileSystem,
  override val entries: List<Zip.Entry>
) : Zip, Closeable by fs {
  class Entry(
    private val root: Path,
    override val path: String,
    override val uncompressedSize: Size,
    override val compressedSize: Size,
    override val zipSize: Size
  ) : Zip.Entry {
    override fun asInput(): Input {
      val child = root.resolve(path)
      if (!child.exists) {
        throw FileNotFoundException("No entry: $path")
      }
      return child.asInput()
    }
  }
}

internal fun ByteString.toZip(): Zip {
  val entries = asInputStream().asZip().mapEntries { name, size, compressedSize, zipSize ->
    BytesZip.Entry(this@toZip, name, size, compressedSize, zipSize)
  }
  return BytesZip(entries)
}

internal class BytesZip(
  override val entries: List<Zip.Entry>
) : Zip {
  class Entry(
    private val bytes: ByteString,
    override val path: String,
    override val uncompressedSize: Size,
    override val compressedSize: Size,
    override val zipSize: Size
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

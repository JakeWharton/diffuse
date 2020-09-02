package com.jakewharton.diffuse.io

import java.io.InputStream
import java.nio.charset.Charset
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.OpenOption
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import okio.Buffer
import okio.ByteString
import okio.ByteString.Companion.toByteString

internal fun InputStream.asZip(charset: Charset = Charsets.UTF_8) = ZipInputStream(this, charset)
internal fun InputStream.readByteString() = readBytes().toByteString()

// TODO https://youtrack.jetbrains.com/issue/KT-18242
internal fun Path.inputStream(vararg options: OpenOption): InputStream = Files.newInputStream(this, *options)
internal val Path.exists get() = Files.exists(this)
internal fun Path.asZipFileSystem(loader: ClassLoader? = null) = FileSystems.newFileSystem(this, loader)!!

internal fun ByteString.asInputStream() = Buffer().write(this).inputStream()

internal fun ZipInputStream.entries(): Sequence<ZipEntry> {
  return object : Sequence<ZipEntry> {
    override fun iterator(): Iterator<ZipEntry> {
      return object : Iterator<ZipEntry> {
        var next: ZipEntry? = null
        var done = false

        override fun hasNext(): Boolean {
          if (done) return false
          if (next == null) {
            next = nextEntry
            if (next == null) {
              done = true
            }
          }
          return next != null
        }

        override fun next(): ZipEntry {
          if (!hasNext()) {
            throw NoSuchElementException()
          }
          return next!!.also { next = null }
        }
      }
    }
  }
}

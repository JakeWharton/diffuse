package com.jakewharton.diffuse.io

import okio.Buffer
import okio.BufferedSource
import okio.ByteString
import okio.buffer
import okio.source
import java.nio.file.Path

interface Input {
  val name: String
  fun source(): BufferedSource

  // Fast-paths to common formats that can be optimized by directly returning or converting inputs.
  fun toByteArray(): ByteArray = source().use(BufferedSource::readByteArray)
  fun toByteString(): ByteString = source().use(BufferedSource::readByteString)
  fun toUtf8(): String = source().use(BufferedSource::readUtf8)

  fun toZip() = toByteString().toZip()

  companion object {
    @JvmStatic
    @JvmName("of")
    fun Path.asInput() = PathInput(this)

    @JvmStatic
    @JvmName("of")
    fun ByteString.asInput(name: String) = BytesInput(name, this)
  }
}

class PathInput internal constructor(
  val path: Path
) : Input {
  override val name get() = path.fileName.toString()
  override fun source() = path.source().buffer()

  override fun toZip() = path.toZip()
}

class BytesInput internal constructor(
  override val name: String,
  val bytes: ByteString
) : Input {
  override fun source(): BufferedSource = Buffer().write(bytes)
  override fun toByteArray() = bytes.toByteArray()
  override fun toByteString() = bytes
  override fun toUtf8() = bytes.utf8()
}

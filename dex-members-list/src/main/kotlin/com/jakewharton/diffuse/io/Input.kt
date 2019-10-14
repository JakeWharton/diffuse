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

  fun toZip() = source().readByteString().toZip()

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
}

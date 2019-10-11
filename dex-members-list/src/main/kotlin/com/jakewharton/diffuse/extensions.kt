package com.jakewharton.diffuse

import com.android.apksig.util.DataSource
import com.android.apksig.util.DataSources
import okio.Buffer
import okio.ByteString
import okio.ByteString.Companion.toByteString
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.util.zip.ZipInputStream

internal fun InputStream.asZip(charset: Charset = Charsets.UTF_8) = ZipInputStream(this, charset)
internal fun InputStream.readByteString() = readBytes().toByteString()

internal fun ByteString.asInputStream() = Buffer().write(this).inputStream()

internal fun ByteArray.asByteBuffer(offset: Int = 0, length: Int = size - offset): ByteBuffer =
  ByteBuffer.wrap(this, offset, length)

internal fun ByteBuffer.asDataSource(): DataSource = DataSources.asDataSource(this)

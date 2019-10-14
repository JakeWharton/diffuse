package com.jakewharton.diffuse

import com.android.apksig.util.DataSource
import com.android.apksig.util.DataSources
import com.google.devrel.gmscore.tools.apk.arsc.BinaryResourceFile
import com.jakewharton.diffuse.io.Input
import okio.Buffer
import okio.BufferedSource
import okio.ByteString
import okio.ByteString.Companion.toByteString
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.OpenOption
import java.nio.file.Path
import java.util.zip.ZipInputStream

internal fun InputStream.asZip(charset: Charset = Charsets.UTF_8) = ZipInputStream(this, charset)
internal fun InputStream.readByteString() = readBytes().toByteString()

internal fun Path.inputStream(vararg options: OpenOption): InputStream = Files.newInputStream(this, *options)
internal val Path.exists get() = Files.exists(this)
internal fun Path.asZipFileSystem(loader: ClassLoader? = null) = FileSystems.newFileSystem(this, loader)!!

internal fun ByteString.asInputStream() = Buffer().write(this).inputStream()

internal fun ByteArray.asByteBuffer(offset: Int = 0, length: Int = size - offset): ByteBuffer =
  ByteBuffer.wrap(this, offset, length)

internal fun ByteBuffer.asDataSource(): DataSource = DataSources.asDataSource(this)

internal fun Input.toBinaryResourceFile(): BinaryResourceFile =
  BinaryResourceFile(source().use(BufferedSource::readByteArray))

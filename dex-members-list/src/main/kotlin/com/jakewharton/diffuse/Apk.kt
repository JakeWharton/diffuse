package com.jakewharton.diffuse

import com.android.apksig.ApkVerifier
import com.android.apksig.util.DataSources
import com.jakewharton.dex.entries
import com.jakewharton.dex.readBytes
import com.jakewharton.diffuse.AndroidManifest.Companion.toAndroidManifest
import com.jakewharton.diffuse.ArchiveFile.Type.Companion.toApkFileType
import com.jakewharton.diffuse.Arsc.Companion.toArsc
import com.jakewharton.diffuse.Dex.Companion.toDex
import okio.Buffer
import okio.ByteString
import okio.ByteString.Companion.toByteString
import java.nio.file.Path
import java.util.SortedMap
import java.util.zip.ZipInputStream

class Apk private constructor(
  override val filename: String?,
  override val bytes: ByteString
) : Binary {
  val files: SortedMap<String, ArchiveFile> by lazy {
    bytes.parseArchiveFiles { it.toApkFileType() }
  }
  val dexes: List<Dex> by lazy {
    ZipInputStream(Buffer().write(bytes).inputStream()).use { zis ->
      zis.entries()
          .filter { it.name.endsWith(".dex") }
          .map { zis.readBytes().toByteString().toDex() }
          .toList()
    }
  }
  val arsc: Arsc by lazy {
    ZipInputStream(Buffer().write(bytes).inputStream()).use { zis ->
      zis.entries().first { it.name.endsWith(".arsc") }
      zis.readBytes().toByteString().toArsc()
    }
  }
  val manifest: AndroidManifest by lazy {
    ZipInputStream(Buffer().write(bytes).inputStream()).use { zis ->
      zis.entries().first { it.name == "AndroidManifest.xml" }
      zis.readBytes().toByteString().toAndroidManifest()
    }
  }
  val signatures: Signatures by lazy {
    val result = ApkVerifier.Builder(DataSources.asDataSource(bytes.asByteBuffer())).build()
        .verify()
    Signatures(
      result.v1SchemeSigners.map { it.certificate.encoded.toByteString().sha1() }.sorted(),
      result.v2SchemeSigners.map { it.certificate.encoded.toByteString().sha1() }.sorted(),
      result.v3SchemeSigners.map { it.certificate.encoded.toByteString().sha1() }.sorted()
    )
  }

  data class Signatures(
    val v1: List<ByteString>,
    val v2: List<ByteString>,
    val v3: List<ByteString>
  )

  companion object {
    @JvmStatic
    @JvmName("create")
    fun Path.toApk() = Apk(fileName.toString(), readBytes().toByteString())
  }
}

internal fun Apk.Signatures.toSummaryString(): String {
  if (v1.isEmpty() && v2.isEmpty() && v3.isEmpty()) {
    return "none"
  }
  return buildString {
    if (v1.isNotEmpty()) {
      append("V1")
      if (v1.size > 1) {
        append(" (x")
        append(v1.size)
        append(')')
      }
    }
    if (v2.isNotEmpty()) {
      if (length > 0) {
        append(", ")
      }
      append("V2")
      if (v2.size > 1) {
        append(" (x")
        append(v2.size)
        append(')')
      }
    }
    if (v3.isNotEmpty()) {
      if (length > 0) {
        append(", ")
      }
      append("V3")
      if (v3.size > 1) {
        append(" (x")
        append(v3.size)
        append(')')
      }
    }
  }
}

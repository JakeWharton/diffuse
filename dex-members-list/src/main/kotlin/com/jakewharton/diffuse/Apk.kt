package com.jakewharton.diffuse

import com.android.apksig.ApkVerifier
import com.android.apksig.util.DataSources
import com.jakewharton.dex.entries
import com.jakewharton.dex.readBytes
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
  val filename: String?,
  val bytes: ByteString
) {
  val files: SortedMap<String, ArchiveFile> by lazy {
    ZipInputStream(Buffer().write(bytes).inputStream()).use { zis ->
      zis.entries()
          .associate { entry ->
            entry.name to ArchiveFile(entry.name, entry.name.toApkFileType(),
                Size(entry.compressedSize),
                Size(entry.size))
          }
          .toSortedMap()
    }
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

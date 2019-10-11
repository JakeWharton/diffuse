package com.jakewharton.diffuse

import com.android.apksig.ApkVerifier
import com.jakewharton.dex.readBytes
import okio.ByteString
import okio.ByteString.Companion.toByteString
import java.nio.file.Path

data class Signatures(
  val v1: List<ByteString>,
  val v2: List<ByteString>,
  val v3: List<ByteString>
) {
  companion object {
    @JvmStatic
    @JvmName("parse")
    fun Path.toSignatures(): Signatures {
      val result = ApkVerifier.Builder(readBytes().asByteBuffer().asDataSource())
          .build()
          .verify()
      return Signatures(
          result.v1SchemeSigners.map { it.certificate.encoded.toByteString().sha1() }.sorted(),
          result.v2SchemeSigners.map { it.certificate.encoded.toByteString().sha1() }.sorted(),
          result.v3SchemeSigners.map { it.certificate.encoded.toByteString().sha1() }.sorted()
      )
    }
  }
}

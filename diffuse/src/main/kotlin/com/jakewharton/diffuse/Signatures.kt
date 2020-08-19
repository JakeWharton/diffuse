package com.jakewharton.diffuse

import com.android.apksig.ApkVerifier
import com.jakewharton.diffuse.io.Input
import okio.ByteString
import okio.ByteString.Companion.toByteString

data class Signatures(
  val v1: List<ByteString>,
  val v2: List<ByteString>,
  val v3: List<ByteString>
) {
  companion object {
    @JvmStatic
    @JvmName("parse")
    fun Input.toSignatures(): Signatures {
      // TODO should be able to make a DataSource from a FileChannel to avoid toByteArray here.
      val dataSource = toByteArray().asByteBuffer().asDataSource()
      val result = ApkVerifier.Builder(dataSource).build().verify()
      return Signatures(
        result.v1SchemeSigners.map { it.certificate.encoded.toByteString().sha1() }.sorted(),
        result.v2SchemeSigners.map { it.certificate.encoded.toByteString().sha1() }.sorted(),
        result.v3SchemeSigners.map { it.certificate.encoded.toByteString().sha1() }.sorted()
      )
    }
  }
}

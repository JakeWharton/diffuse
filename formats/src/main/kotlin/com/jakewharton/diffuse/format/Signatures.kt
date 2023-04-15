package com.jakewharton.diffuse.format

import com.android.apksig.ApkVerifier
import com.android.apksig.util.DataSource
import com.android.apksig.util.DataSources
import com.jakewharton.diffuse.io.Input
import com.jakewharton.diffuse.io.PathInput
import java.io.Closeable
import java.nio.channels.FileChannel
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import okio.ByteString
import okio.ByteString.Companion.toByteString

data class Signatures(
  val v1: List<ByteString>,
  val v2: List<ByteString>,
  val v3: List<ByteString>,
  val v4: List<ByteString>,
) {
  companion object {
    @JvmStatic
    @JvmName("parse")
    fun Input.toSignatures(): Signatures {
      useDataSource { dataSource ->
        val result = ApkVerifier.Builder(dataSource).build().verify()
        return Signatures(
          result.v1SchemeSigners.map { it.certificate.encoded.toByteString().sha1() }.sorted(),
          result.v2SchemeSigners.map { it.certificate.encoded.toByteString().sha1() }.sorted(),
          result.v3SchemeSigners.map { it.certificate.encoded.toByteString().sha1() }.sorted(),
          result.v4SchemeSigners.map { it.certificate.encoded.toByteString().sha1() }.sorted(),
        )
      }
    }

    private inline fun Input.useDataSource(body: (DataSource) -> Unit) {
      contract {
        callsInPlace(body, InvocationKind.EXACTLY_ONCE)
      }
      var closeable: Closeable? = null
      val source = when (this) {
        is PathInput -> {
          val channel = FileChannel.open(path)
          closeable = channel
          DataSources.asDataSource(channel)
        }
        else -> DataSources.asDataSource(toByteString().asByteBuffer())
      }
      try {
        body(source)
      } finally {
        closeable?.close()
      }
    }
  }
}

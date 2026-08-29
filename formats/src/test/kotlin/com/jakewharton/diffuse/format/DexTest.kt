package com.jakewharton.diffuse.format

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.prop
import com.jakewharton.diffuse.format.Dex.Companion.toDex
import com.jakewharton.diffuse.io.Input.Companion.asInput
import com.jakewharton.diffuse.testing.requireResource
import okio.ByteString.Companion.toByteString
import org.junit.Test

class DexTest {
  @Test
  fun parameterTypeIsUnsigned() {
    val dex = DexTest::class.java.requireResource("/params32769.dex").asInput().toDex()
    assertThat(dex).prop(Dex::declaredMembers).hasSize(32769)
  }

  @Test
  fun formatVersion() {
    val bytes = DexTest::class.java.requireResource("/params32769.dex").asInput().toByteArray()
    val dex35 = bytes.toByteString().asInput("dex35.dex").toDex()
    assertThat(dex35.formatVersion).isEqualTo(35)

    val dex38 =
      bytes
        .clone()
        .apply {
          set(4, '0'.code.toByte())
          set(5, '3'.code.toByte())
          set(6, '8'.code.toByte())
        }
        .toByteString()
        .asInput("dex38.dex")
        .toDex()
    assertThat(dex38.formatVersion).isEqualTo(38)
  }
}

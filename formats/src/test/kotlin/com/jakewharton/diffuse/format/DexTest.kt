package com.jakewharton.diffuse.format

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.prop
import com.jakewharton.diffuse.format.Dex.Companion.toDex
import org.junit.Test

class DexTest {
  @Test fun parameterTypeIsUnsigned() {
    val dex = DexTest::class.java.getResource("/params32769.dex")
      .asInput()
      .toDex()
    assertThat(dex)
      .prop(Dex::declaredMembers)
      .hasSize(32769)
  }
}

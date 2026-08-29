package com.jakewharton.diffuse

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.diffuse.diff.DexDiff
import com.jakewharton.diffuse.diff.toDetailReport
import com.jakewharton.diffuse.format.Dex
import com.jakewharton.diffuse.format.Dex.Companion.toDex
import com.jakewharton.diffuse.io.Input.Companion.asInput
import com.jakewharton.diffuse.testing.requireResource
import okio.ByteString.Companion.toByteString
import org.junit.Test

class DexDiffTest {
  @Test
  fun formatVersionsSingleDexSame() {
    val diff =
      DexDiff(
        oldDexes = listOf(Dex("classes.dex", 35)),
        newDexes = listOf(Dex("classes.dex", 35)),
      )

    assertThat(diff.changed).isEqualTo(false)
    assertThat(diff.toDetailReport()).isEqualTo("")
  }

  @Test
  fun formatVersionsSingleDexChanged() {
    val diff =
      DexDiff(
        oldDexes = listOf(Dex("classes.dex", 35)),
        newDexes = listOf(Dex("classes.dex", 38)),
      )

    assertThat(diff.changed).isEqualTo(true)
    assertThat(diff.toDetailReport())
      .isEqualTo(
        """
        |
        |FORMAT VERSIONS:
        |
        |   old │ new │ diff      
        |  ─────┼─────┼───────────
        |   1   │ 1   │ 0 (+1 -1) 
        |  
        |  + classes.dex: 38
        |  
        |  - classes.dex: 35
        |  
        |"""
          .trimMargin()
      )
  }

  @Test
  fun formatVersionsMultidexChanged() {
    val diff =
      DexDiff(
        oldDexes = listOf(Dex("classes.dex", 35), Dex("classes2.dex", 35)),
        newDexes = listOf(Dex("classes.dex", 35), Dex("classes2.dex", 38)),
      )

    assertThat(diff.changed).isEqualTo(true)
    assertThat(diff.toDetailReport())
      .isEqualTo(
        """
        |
        |FORMAT VERSIONS:
        |
        |   old │ new │ diff      
        |  ─────┼─────┼───────────
        |   2   │ 2   │ 0 (+1 -1) 
        |  
        |  + classes2.dex: 38
        |  
        |  - classes2.dex: 35
        |  
        |"""
          .trimMargin()
      )
  }
}

private fun Dex(name: String, version: Int): Dex {
  val (tens, ones) = version.toString().toCharArray()
  return DexDiffTest::class
    .java
    .requireResource("/params32769.dex")
    .readBytes()
    .apply {
      set(4, '0'.code.toByte())
      set(5, tens.code.toByte())
      set(6, ones.code.toByte())
    }
    .toByteString()
    .asInput(name)
    .toDex()
}

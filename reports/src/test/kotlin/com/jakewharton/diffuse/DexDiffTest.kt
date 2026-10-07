package com.jakewharton.diffuse

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
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

    assertThat(diff.changed).isFalse()
    assertThat(diff.toDetailReport()).isEqualTo("")
  }

  @Test
  fun formatVersionsSingleDexChanged() {
    val diff =
      DexDiff(
        oldDexes = listOf(Dex("classes.dex", 35)),
        newDexes = listOf(Dex("classes.dex", 38)),
      )

    assertThat(diff.changed).isTrue()
    assertThat(diff.toDetailReport())
      .isEqualTo(
        """
        |
        |FORMAT VERSIONS:
        |
        |   version │ old │ new │ diff       
        |  ─────────┼─────┼─────┼────────────
        |        35 │   1 │   0 │ -1 (+0 -1) 
        |        38 │   0 │   1 │ +1 (+1 -0) 
        |
        |  classes.dex: 35 → 38
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

    assertThat(diff.changed).isTrue()
    assertThat(diff.toDetailReport())
      .isEqualTo(
        """
        |
        |FORMAT VERSIONS:
        |
        |   version │ old │ new │ diff       
        |  ─────────┼─────┼─────┼────────────
        |        35 │   2 │   1 │ -1 (+0 -1) 
        |        38 │   0 │   1 │ +1 (+1 -0) 
        |
        |  classes2.dex: 35 → 38
        |"""
          .trimMargin()
      )
  }

  @Test
  fun formatVersionsMultidexAdded() {
    val diff =
      DexDiff(
        oldDexes = listOf(Dex("classes.dex", 35)),
        newDexes = listOf(Dex("classes.dex", 35), Dex("classes2.dex", 38)),
      )

    assertThat(diff.changed).isTrue()
    assertThat(diff.toDetailReport())
      .isEqualTo(
        """
        |
        |FORMAT VERSIONS:
        |
        |   version │ old │ new │ diff       
        |  ─────────┼─────┼─────┼────────────
        |        38 │   0 │   1 │ +1 (+1 -0) 
        |"""
          .trimMargin()
      )
  }

  @Test
  fun formatVersionsMultidexRemoved() {
    val diff =
      DexDiff(
        oldDexes = listOf(Dex("classes.dex", 35), Dex("classes2.dex", 38)),
        newDexes = listOf(Dex("classes.dex", 35)),
      )

    assertThat(diff.changed).isTrue()
    assertThat(diff.toDetailReport())
      .isEqualTo(
        """
        |
        |FORMAT VERSIONS:
        |
        |   version │ old │ new │ diff       
        |  ─────────┼─────┼─────┼────────────
        |        38 │   1 │   0 │ -1 (+0 -1) 
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

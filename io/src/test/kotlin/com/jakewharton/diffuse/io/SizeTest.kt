package com.jakewharton.diffuse.io

import assertk.assertThat
import assertk.assertions.hasToString
import assertk.assertions.isEqualTo
import org.junit.Test

class SizeTest {
  @Test
  fun toStringFormatsBytes() {
    assertThat(Size(0)).hasToString("0 B")
    assertThat(Size(1)).hasToString("1 B")
    assertThat(Size(-1)).hasToString("-1 B")
    assertThat(Size(1024)).hasToString("1 KiB")
    assertThat(Size(-1024)).hasToString("-1 KiB")
    assertThat(Size(1024L * 1024)).hasToString("1 MiB")
    assertThat(Size(-(1024L * 1024))).hasToString("-1 MiB")
    assertThat(Size(1024L * 1024 * 1024)).hasToString("1 GiB")
    assertThat(Size(-(1024L * 1024 * 1024))).hasToString("-1 GiB")
  }

  @Test
  fun toStringDecimalFormatsBytes() {
    assertThat(Size(0).toString(ByteUnit.Decimal)).isEqualTo("0 B")
    assertThat(Size(1).toString(ByteUnit.Decimal)).isEqualTo("1 B")
    assertThat(Size(-1).toString(ByteUnit.Decimal)).isEqualTo("-1 B")
    assertThat(Size(1000).toString(ByteUnit.Decimal)).isEqualTo("1 KB")
    assertThat(Size(-1000).toString(ByteUnit.Decimal)).isEqualTo("-1 KB")
    assertThat(Size(1000L * 1000).toString(ByteUnit.Decimal)).isEqualTo("1 MB")
    assertThat(Size(-(1000L * 1000)).toString(ByteUnit.Decimal)).isEqualTo("-1 MB")
    assertThat(Size(1000L * 1000 * 1000).toString(ByteUnit.Decimal)).isEqualTo("1 GB")
    assertThat(Size(-(1000L * 1000 * 1000)).toString(ByteUnit.Decimal)).isEqualTo("-1 GB")
  }
}

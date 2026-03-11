package com.jakewharton.diffuse.io

import assertk.assertThat
import assertk.assertions.hasToString
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
}

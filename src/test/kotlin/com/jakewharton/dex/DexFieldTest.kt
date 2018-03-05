package com.jakewharton.dex

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DexFieldTest {
  @Test fun string() {
    val method = DexField("com.example.Foo", "bar", "com.example.Bar")
    assertThat(method.toString()).isEqualTo("com.example.Foo bar: Bar")
  }
}

package com.jakewharton.dex

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DexMemberTest {
  @Test fun compareInSameClass() {
    val field = DexField("com.example.Foo", "bar", "com.example.Bar")
    val method = DexMethod("com.example.Foo", "bar", emptyList(), "com.example.Bar")
    assertThat(method < field).isTrue()
    assertThat(field < method).isFalse()
  }

  @Test fun compareInDifferentClass() {
    val field = DexField("com.example.Boo", "bar", "com.example.Bar")
    val method = DexMethod("com.example.Foo", "bar", emptyList(), "com.example.Bar")
    assertThat(method < field).isFalse()
    assertThat(field < method).isTrue()
  }
}

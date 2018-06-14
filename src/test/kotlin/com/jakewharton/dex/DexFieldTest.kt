package com.jakewharton.dex

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DexFieldTest {
  @Test fun string() {
    val method = DexField("com.example.Foo", "bar", "com.example.Bar")
    assertThat(method.toString()).isEqualTo("com.example.Foo bar: Bar")
  }

  @Test fun compareToSame() {
    val one = DexField("com.example.Foo", "bar", "com.example.Bar")
    val two = DexField("com.example.Foo", "bar", "com.example.Bar")
    assertThat(one < two).isFalse()
    assertThat(two < one).isFalse()
  }

  @Test fun compareToDifferentDeclaringType() {
    val one = DexField("com.example.Bar", "bar", "com.example.Bar")
    val two = DexField("com.example.Foo", "bar", "com.example.Bar")
    assertThat(one < two).isTrue()
    assertThat(two < one).isFalse()
  }

  @Test fun compareToDifferentName() {
    val one = DexField("com.example.Foo", "bar", "com.example.Bar")
    val two = DexField("com.example.Foo", "foo", "com.example.Bar")
    assertThat(one < two).isTrue()
    assertThat(two < one).isFalse()
  }

  @Test fun compareToDifferentType() {
    val one = DexField("com.example.Foo", "bar", "com.example.Bar")
    val two = DexField("com.example.Foo", "bar", "com.example.Foo")
    assertThat(one < two).isTrue()
    assertThat(two < one).isFalse()
  }
}

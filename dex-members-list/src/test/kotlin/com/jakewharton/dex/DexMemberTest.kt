package com.jakewharton.dex

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DexMemberTest {
  private val fooDescriptor = TypeDescriptor("Lcom/example/Foo;")
  private val barDescriptor = TypeDescriptor("Lcom/example/Bar;")

  @Test fun compareInSameClass() {
    val field = DexField(fooDescriptor, "bar", barDescriptor)
    val method = DexMethod(fooDescriptor, "bar", emptyList(), barDescriptor)
    assertThat(method < field).isTrue()
    assertThat(field < method).isFalse()
  }

  @Test fun compareInDifferentClass() {
    val field = DexField(barDescriptor, "bar", barDescriptor)
    val method = DexMethod(fooDescriptor, "bar", emptyList(), barDescriptor)
    assertThat(method < field).isFalse()
    assertThat(field < method).isTrue()
  }
}

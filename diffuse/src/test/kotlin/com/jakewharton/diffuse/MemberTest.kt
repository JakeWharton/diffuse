package com.jakewharton.diffuse

import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.jakewharton.diffuse.format.Field
import com.jakewharton.diffuse.format.Method
import com.jakewharton.diffuse.format.TypeDescriptor
import org.junit.Test

class MemberTest {
  private val fooDescriptor = TypeDescriptor("Lcom/example/Foo;")
  private val barDescriptor = TypeDescriptor("Lcom/example/Bar;")

  @Test fun compareInSameClass() {
    val field = Field(fooDescriptor, "bar", barDescriptor)
    val method = Method(fooDescriptor, "bar", emptyList(), barDescriptor)
    assertThat(method < field).isTrue()
    assertThat(field < method).isFalse()
  }

  @Test fun compareInDifferentClass() {
    val field = Field(barDescriptor, "bar", barDescriptor)
    val method = Method(fooDescriptor, "bar", emptyList(), barDescriptor)
    assertThat(method < field).isFalse()
    assertThat(field < method).isTrue()
  }
}

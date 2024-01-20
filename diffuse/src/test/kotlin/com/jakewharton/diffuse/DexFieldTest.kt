package com.jakewharton.diffuse

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.jakewharton.diffuse.format.Field
import com.jakewharton.diffuse.format.TypeDescriptor
import org.junit.Test

class DexFieldTest {
  private val fooDescriptor = TypeDescriptor("Lcom/example/Foo;")
  private val barDescriptor = TypeDescriptor("Lcom/example/Bar;")

  @Test fun string() {
    val field = Field(fooDescriptor, "bar", barDescriptor)
    assertThat(field.toString()).isEqualTo("com.example.Foo bar: Bar")
  }

  @Test fun renderKotlinLambdaClassName() {
    val field = Field(TypeDescriptor("Lcom/example/Foo$\$Lambda$26;"), "bar", barDescriptor)
    assertThat(field.toString()).isEqualTo("com.example.Foo$\$Lambda$26 bar: Bar")
  }

  @Test fun compareToSame() {
    val one = Field(fooDescriptor, "bar", barDescriptor)
    val two = Field(fooDescriptor, "bar", barDescriptor)
    assertThat(one < two).isFalse()
    assertThat(two < one).isFalse()
  }

  @Test fun compareToDifferentDeclaringType() {
    val one = Field(barDescriptor, "bar", barDescriptor)
    val two = Field(fooDescriptor, "bar", barDescriptor)
    assertThat(one < two).isTrue()
    assertThat(two < one).isFalse()
  }

  @Test fun compareToDifferentName() {
    val one = Field(fooDescriptor, "bar", barDescriptor)
    val two = Field(fooDescriptor, "foo", barDescriptor)
    assertThat(one < two).isTrue()
    assertThat(two < one).isFalse()
  }

  @Test fun compareToDifferentType() {
    val one = Field(fooDescriptor, "bar", barDescriptor)
    val two = Field(fooDescriptor, "bar", fooDescriptor)
    assertThat(one < two).isTrue()
    assertThat(two < one).isFalse()
  }
}

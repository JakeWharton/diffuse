package com.jakewharton.dex

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DexFieldTest {
  private val fooDescriptor = TypeDescriptor("Lcom/example/Foo;")
  private val barDescriptor = TypeDescriptor("Lcom/example/Bar;")

  @Test fun string() {
    val field = DexField(fooDescriptor, "bar", barDescriptor)
    assertThat(field.toString()).apply {
      isEqualTo("com.example.Foo bar: Bar")
      isEqualTo(field.toString(false))
    }
  }

  @Test fun renderKotlinLambdaClassName() {
    val field = DexField(TypeDescriptor("Lcom/example/Foo$\$Lambda$26;"), "bar", barDescriptor)
    assertThat(field.toString()).isEqualTo("com.example.Foo$\$Lambda$26 bar: Bar")
    assertThat(field.toString(hideSyntheticNumbers = true))
        .isEqualTo("com.example.Foo$\$Lambda bar: Bar")
  }

  @Test fun compareToSame() {
    val one = DexField(fooDescriptor, "bar", barDescriptor)
    val two = DexField(fooDescriptor, "bar", barDescriptor)
    assertThat(one < two).isFalse()
    assertThat(two < one).isFalse()
  }

  @Test fun compareToDifferentDeclaringType() {
    val one = DexField(barDescriptor, "bar", barDescriptor)
    val two = DexField(fooDescriptor, "bar", barDescriptor)
    assertThat(one < two).isTrue()
    assertThat(two < one).isFalse()
  }

  @Test fun compareToDifferentName() {
    val one = DexField(fooDescriptor, "bar", barDescriptor)
    val two = DexField(fooDescriptor, "foo", barDescriptor)
    assertThat(one < two).isTrue()
    assertThat(two < one).isFalse()
  }

  @Test fun compareToDifferentType() {
    val one = DexField(fooDescriptor, "bar", barDescriptor)
    val two = DexField(fooDescriptor, "bar", fooDescriptor)
    assertThat(one < two).isTrue()
    assertThat(two < one).isFalse()
  }
}

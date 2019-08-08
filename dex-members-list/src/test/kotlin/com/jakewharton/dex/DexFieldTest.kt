package com.jakewharton.dex

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DexFieldTest {
  private val fooDescriptor = Descriptor("Lcom/example/Foo;")
  private val barDescriptor = Descriptor("Lcom/example/Bar;")

  @Test fun string() {
    val field = DexField(fooDescriptor, "bar", barDescriptor)
    assertThat(field.toString()).isEqualTo(field.render())
  }

  @Test fun render() {
    val field = DexField(fooDescriptor, "bar", barDescriptor)
    assertThat(field.render()).isEqualTo("com.example.Foo bar: Bar")
  }

  @Test fun renderKotlinLambdaClassName() {
    val field = DexField(Descriptor("Lcom/example/Foo$\$Lambda$26;"), "bar", barDescriptor)
    assertThat(field.render()).isEqualTo("com.example.Foo$\$Lambda$26 bar: Bar")
  }

  @Test fun renderKotlinLambdaClassNameHidingSynthetics() {
    val field = DexField(Descriptor("Lcom/example/Foo$\$Lambda$26;"), "bar", barDescriptor)
    assertThat(field.render(hideSyntheticNumbers = true))
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

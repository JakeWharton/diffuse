package com.jakewharton.dex

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DexFieldTest {
  @Test fun string() {
    val field = DexField("com.example.Foo", "bar", "com.example.Bar")
    assertThat(field.toString()).isEqualTo(field.render())
  }

  @Test fun render() {
    val field = DexField("com.example.Foo", "bar", "com.example.Bar")
    assertThat(field.render()).isEqualTo("com.example.Foo bar: Bar")
  }

  @Test fun renderKotlinLambdaClassName() {
    val field = DexField("com.example.Foo$\$Lambda$26", "bar", "com.example.Bar")
    assertThat(field.render()).isEqualTo("com.example.Foo$\$Lambda$26 bar: Bar")
  }

  @Test fun renderKotlinLambdaClassNameHidingSynthetics() {
    val field = DexField("com.example.Foo$\$Lambda$26", "bar", "com.example.Bar")
    assertThat(field.render(hideSyntheticNumbers = true))
        .isEqualTo("com.example.Foo$\$Lambda bar: Bar")
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

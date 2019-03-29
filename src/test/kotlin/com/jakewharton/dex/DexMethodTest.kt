package com.jakewharton.dex

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DexMethodTest {
  @Test fun toStringCallsRender() {
    val method = DexMethod("com.example.Foo", "bar", emptyList(), "com.example.Bar")
    assertThat(method.toString()).isEqualTo(method.render())
  }

  @Test fun render() {
    val method = DexMethod("com.example.Foo", "bar", emptyList(), "com.example.Bar")
    assertThat(method.render()).isEqualTo("com.example.Foo bar() → Bar")
  }

  @Test fun renderVoidReturnType() {
    val method = DexMethod("com.example.Foo", "bar", emptyList(), "void")
    assertThat(method.render()).isEqualTo("com.example.Foo bar()")
  }

  @Test fun renderWithSynthetics() {
    val synthetic = DexMethod("com.example.Foo", "access$000", emptyList(), "com.example.Bar")
    assertThat(synthetic.render()).isEqualTo("com.example.Foo access$000() → Bar")
  }

  @Test fun renderHidingSynthetics() {
    val method = DexMethod("com.example.Foo", "bar", emptyList(), "com.example.Bar")
    assertThat(method.render(hideSyntheticNumbers = true))
        .isEqualTo("com.example.Foo bar() → Bar")

    val synthetic = DexMethod("com.example.Foo", "access$000", emptyList(), "com.example.Bar")
    assertThat(synthetic.render(hideSyntheticNumbers = true))
        .isEqualTo("com.example.Foo access() → Bar")
  }

  @Test fun renderKotlinLambdaFunctionName() {
    val synthetic = DexMethod("com.example.Foo", "lambda\$refreshSessionToken$14\$MainActivity", emptyList(), "com.example.Bar")
    assertThat(synthetic.render()).isEqualTo("com.example.Foo lambda\$refreshSessionToken$14\$MainActivity() → Bar")
  }

  @Test fun renderKotlinLambdaFunctionNameHidingSynthetics() {
    val method = DexMethod("com.example.Foo", "bar", emptyList(), "com.example.Bar")
    assertThat(method.render(hideSyntheticNumbers = true))
        .isEqualTo("com.example.Foo bar() → Bar")

    val synthetic = DexMethod("com.example.Foo", "lambda\$refreshSessionToken$14\$MainActivity", emptyList(), "com.example.Bar")
    assertThat(synthetic.render(hideSyntheticNumbers = true))
        .isEqualTo("com.example.Foo lambda\$refreshSessionToken\$MainActivity() → Bar")
  }

  @Test fun renderKotlinLambdaClassName() {
    val synthetic = DexMethod("com.example.Foo$\$Lambda$26", "bar", emptyList(), "com.example.Bar")
    assertThat(synthetic.render()).isEqualTo("com.example.Foo$\$Lambda$26 bar() → Bar")
  }

  @Test fun renderKotlinLambdaClassNameHidingSynthetics() {
    val synthetic = DexMethod("com.example.Foo$\$Lambda$26", "bar", emptyList(), "com.example.Bar")
    assertThat(synthetic.render(hideSyntheticNumbers = true))
        .isEqualTo("com.example.Foo$\$Lambda bar() → Bar")
  }

  @Test fun compareToSame() {
    val one = DexMethod("com.example.Foo", "bar", emptyList(), "com.example.Bar")
    val two = DexMethod("com.example.Foo", "bar", emptyList(), "com.example.Bar")
    assertThat(one < two).isFalse()
    assertThat(two < one).isFalse()
  }

  @Test fun compareToDifferentDeclaringType() {
    val one = DexMethod("com.example.Bar", "bar", emptyList(), "com.example.Bar")
    val two = DexMethod("com.example.Foo", "bar", emptyList(), "com.example.Bar")
    assertThat(one < two).isTrue()
    assertThat(two < one).isFalse()
  }

  @Test fun compareToDifferentName() {
    val one = DexMethod("com.example.Foo", "bar", emptyList(), "com.example.Bar")
    val two = DexMethod("com.example.Foo", "foo", emptyList(), "com.example.Bar")
    assertThat(one < two).isTrue()
    assertThat(two < one).isFalse()
  }

  @Test fun compareToDifferentParameterList() {
    val one = DexMethod("com.example.Foo", "bar", emptyList(), "com.example.Bar")
    val two = DexMethod("com.example.Foo", "bar", listOf("int"), "com.example.Bar")
    assertThat(one < two).isTrue()
    assertThat(two < one).isFalse()
  }

  @Test fun compareToDifferentReturnType() {
    val one = DexMethod("com.example.Foo", "bar", emptyList(), "com.example.Bar")
    val two = DexMethod("com.example.Foo", "bar", emptyList(), "com.example.Foo")
    assertThat(one < two).isTrue()
    assertThat(two < one).isFalse()
  }
}

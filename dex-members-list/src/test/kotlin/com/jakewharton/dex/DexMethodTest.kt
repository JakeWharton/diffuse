package com.jakewharton.dex

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DexMethodTest {
  private val fooDescriptor = TypeDescriptor("Lcom/example/Foo;")
  private val barDescriptor = TypeDescriptor("Lcom/example/Bar;")

  @Test fun string() {
    val method = DexMethod(fooDescriptor, "bar", emptyList(), barDescriptor)
    assertThat(method.toString()).isEqualTo("com.example.Foo bar() → Bar")
  }

  @Test fun renderVoidReturnType() {
    val method = DexMethod(fooDescriptor, "bar", emptyList(), TypeDescriptor("V"))
    assertThat(method.toString()).isEqualTo("com.example.Foo bar()")
  }

  @Test fun renderWithSynthetics() {
    val synthetic = DexMethod(fooDescriptor, "access$000", emptyList(), barDescriptor)
    assertThat(synthetic.toString()).isEqualTo("com.example.Foo access$000() → Bar")
  }

  @Test fun renderKotlinLambdaFunctionName() {
    val synthetic = DexMethod(fooDescriptor, "lambda\$refreshSessionToken$14\$MainActivity", emptyList(), barDescriptor)
    assertThat(synthetic.toString()).isEqualTo("com.example.Foo lambda\$refreshSessionToken$14\$MainActivity() → Bar")
  }

  @Test fun renderKotlinLambdaClassName() {
    val synthetic = DexMethod(TypeDescriptor("Lcom/example/Foo$\$Lambda$26;"), "bar", emptyList(), barDescriptor)
    assertThat(synthetic.toString()).isEqualTo("com.example.Foo$\$Lambda$26 bar() → Bar")
  }

  @Test fun compareToSame() {
    val one = DexMethod(fooDescriptor, "bar", emptyList(), barDescriptor)
    val two = DexMethod(fooDescriptor, "bar", emptyList(), barDescriptor)
    assertThat(one < two).isFalse()
    assertThat(two < one).isFalse()
  }

  @Test fun compareToDifferentDeclaringType() {
    val one = DexMethod(barDescriptor, "bar", emptyList(), barDescriptor)
    val two = DexMethod(fooDescriptor, "bar", emptyList(), barDescriptor)
    assertThat(one < two).isTrue()
    assertThat(two < one).isFalse()
  }

  @Test fun compareToDifferentName() {
    val one = DexMethod(fooDescriptor, "bar", emptyList(), barDescriptor)
    val two = DexMethod(fooDescriptor, "foo", emptyList(), barDescriptor)
    assertThat(one < two).isTrue()
    assertThat(two < one).isFalse()
  }

  @Test fun compareToDifferentParameterList() {
    val one = DexMethod(fooDescriptor, "bar", emptyList(), barDescriptor)
    val two = DexMethod(fooDescriptor, "bar", listOf(TypeDescriptor("I")), barDescriptor)
    assertThat(one < two).isTrue()
    assertThat(two < one).isFalse()
  }

  @Test fun compareToDifferentReturnType() {
    val one = DexMethod(fooDescriptor, "bar", emptyList(), barDescriptor)
    val two = DexMethod(fooDescriptor, "bar", emptyList(), fooDescriptor)
    assertThat(one < two).isTrue()
    assertThat(two < one).isFalse()
  }
}

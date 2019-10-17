package com.jakewharton.diffuse

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DexMethodTest {
  private val fooDescriptor = TypeDescriptor("Lcom/example/Foo;")
  private val barDescriptor = TypeDescriptor("Lcom/example/Bar;")

  @Test fun string() {
    val method = Method(fooDescriptor, "bar", emptyList(), barDescriptor)
    assertThat(method.toString()).isEqualTo("com.example.Foo bar() → Bar")
  }

  @Test fun renderVoidReturnType() {
    val method = Method(fooDescriptor, "bar", emptyList(), TypeDescriptor("V"))
    assertThat(method.toString()).isEqualTo("com.example.Foo bar()")
  }

  @Test fun renderWithSynthetics() {
    val synthetic = Method(fooDescriptor, "access$000", emptyList(), barDescriptor)
    assertThat(synthetic.toString()).isEqualTo("com.example.Foo access$000() → Bar")
  }

  @Test fun renderKotlinLambdaFunctionName() {
    val synthetic = Method(fooDescriptor, "lambda\$refreshSessionToken$14\$MainActivity", emptyList(), barDescriptor)
    assertThat(synthetic.toString()).isEqualTo("com.example.Foo lambda\$refreshSessionToken$14\$MainActivity() → Bar")
  }

  @Test fun renderKotlinLambdaClassName() {
    val synthetic = Method(TypeDescriptor("Lcom/example/Foo$\$Lambda$26;"), "bar", emptyList(), barDescriptor)
    assertThat(synthetic.toString()).isEqualTo("com.example.Foo$\$Lambda$26 bar() → Bar")
  }

  @Test fun compareToSame() {
    val one = Method(fooDescriptor, "bar", emptyList(), barDescriptor)
    val two = Method(fooDescriptor, "bar", emptyList(), barDescriptor)
    assertThat(one < two).isFalse()
    assertThat(two < one).isFalse()
  }

  @Test fun compareToDifferentDeclaringType() {
    val one = Method(barDescriptor, "bar", emptyList(), barDescriptor)
    val two = Method(fooDescriptor, "bar", emptyList(), barDescriptor)
    assertThat(one < two).isTrue()
    assertThat(two < one).isFalse()
  }

  @Test fun compareToDifferentName() {
    val one = Method(fooDescriptor, "bar", emptyList(), barDescriptor)
    val two = Method(fooDescriptor, "foo", emptyList(), barDescriptor)
    assertThat(one < two).isTrue()
    assertThat(two < one).isFalse()
  }

  @Test fun compareToDifferentParameterList() {
    val one = Method(fooDescriptor, "bar", emptyList(), barDescriptor)
    val two = Method(fooDescriptor, "bar", listOf(TypeDescriptor("I")), barDescriptor)
    assertThat(one < two).isTrue()
    assertThat(two < one).isFalse()
  }

  @Test fun compareToDifferentReturnType() {
    val one = Method(fooDescriptor, "bar", emptyList(), barDescriptor)
    val two = Method(fooDescriptor, "bar", emptyList(), fooDescriptor)
    assertThat(one < two).isTrue()
    assertThat(two < one).isFalse()
  }
}

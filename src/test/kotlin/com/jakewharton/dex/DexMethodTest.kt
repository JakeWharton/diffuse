package com.jakewharton.dex

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DexMethodTest {
  private val fooDescriptor = Descriptor("Lcom/example/Foo;")
  private val barDescriptor = Descriptor("Lcom/example/Bar;")

  @Test fun toStringCallsRender() {
    val method = DexMethod(fooDescriptor, "bar", emptyList(), barDescriptor)
    assertThat(method.toString()).isEqualTo(method.render())
  }

  @Test fun render() {
    val method = DexMethod(fooDescriptor, "bar", emptyList(), barDescriptor)
    assertThat(method.render()).isEqualTo("com.example.Foo bar() → Bar")
  }

  @Test fun renderVoidReturnType() {
    val method = DexMethod(fooDescriptor, "bar", emptyList(), Descriptor.VOID)
    assertThat(method.render()).isEqualTo("com.example.Foo bar()")
  }

  @Test fun renderWithSynthetics() {
    val synthetic = DexMethod(fooDescriptor, "access$000", emptyList(), barDescriptor)
    assertThat(synthetic.render()).isEqualTo("com.example.Foo access$000() → Bar")
  }

  @Test fun renderHidingSynthetics() {
    val method = DexMethod(fooDescriptor, "bar", emptyList(), barDescriptor)
    assertThat(method.render(hideSyntheticNumbers = true))
        .isEqualTo("com.example.Foo bar() → Bar")

    val synthetic = DexMethod(fooDescriptor, "access$000", emptyList(), barDescriptor)
    assertThat(synthetic.render(hideSyntheticNumbers = true))
        .isEqualTo("com.example.Foo access() → Bar")
  }

  @Test fun renderKotlinLambdaFunctionName() {
    val synthetic = DexMethod(fooDescriptor, "lambda\$refreshSessionToken$14\$MainActivity", emptyList(), barDescriptor)
    assertThat(synthetic.render()).isEqualTo("com.example.Foo lambda\$refreshSessionToken$14\$MainActivity() → Bar")
  }

  @Test fun renderKotlinLambdaFunctionNameHidingSynthetics() {
    val method = DexMethod(fooDescriptor, "bar", emptyList(), barDescriptor)
    assertThat(method.render(hideSyntheticNumbers = true))
        .isEqualTo("com.example.Foo bar() → Bar")

    val synthetic = DexMethod(fooDescriptor, "lambda\$refreshSessionToken$14\$MainActivity", emptyList(), barDescriptor)
    assertThat(synthetic.render(hideSyntheticNumbers = true))
        .isEqualTo("com.example.Foo lambda\$refreshSessionToken\$MainActivity() → Bar")
  }

  @Test fun renderKotlinLambdaClassName() {
    val synthetic = DexMethod(Descriptor("Lcom/example/Foo$\$Lambda$26;"), "bar", emptyList(), barDescriptor)
    assertThat(synthetic.render()).isEqualTo("com.example.Foo$\$Lambda$26 bar() → Bar")
  }

  @Test fun renderKotlinLambdaClassNameHidingSynthetics() {
    val synthetic = DexMethod(Descriptor("Lcom/example/Foo$\$Lambda$26;"), "bar", emptyList(), barDescriptor)
    assertThat(synthetic.render(hideSyntheticNumbers = true))
        .isEqualTo("com.example.Foo$\$Lambda bar() → Bar")
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
    val two = DexMethod(fooDescriptor, "bar", listOf(Descriptor("I")), barDescriptor)
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

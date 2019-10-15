package com.jakewharton.dex

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DexMembersTest {
  private val fooDescriptor = TypeDescriptor("Lcom/example/Foo;")
  private val barDescriptor = TypeDescriptor("Lcom/example/Bar;")

  @Test fun fieldTypeLambdaNumberRemoved() {
    val field = DexField(TypeDescriptor("Lcom/example/Foo$\$Lambda$26;"), "bar", barDescriptor)
    val fieldWithoutLambdaNumber = DexField(TypeDescriptor("Lcom/example/Foo$\$Lambda;"), "bar", barDescriptor)
    assertThat(field.withoutSyntheticSuffix()).isEqualTo(fieldWithoutLambdaNumber)
  }

  @Test fun methodNameSyntheticNumberRemoved() {
    val method = DexMethod(fooDescriptor, "access$000", emptyList(), barDescriptor)
    val methodWithoutSyntheticNumber = DexMethod(fooDescriptor, "access", emptyList(), barDescriptor)
    assertThat(method.withoutSyntheticSuffix()).isEqualTo(methodWithoutSyntheticNumber)
  }

  @Test fun methodNameLambdaNumberRemoved() {
    val method = DexMethod(fooDescriptor, "lambda\$refreshSessionToken$14\$MainActivity", emptyList(), barDescriptor)
    val methodWithoutLambdaNumber = DexMethod(fooDescriptor, "lambda\$refreshSessionToken\$MainActivity", emptyList(), barDescriptor)
    assertThat(method.withoutSyntheticSuffix()).isEqualTo(methodWithoutLambdaNumber)
  }

  @Test fun methodTypeLambdaNumberRemoved() {
    val method = DexMethod(TypeDescriptor("Lcom/example/Foo$\$Lambda$26;"), "bar", emptyList(), barDescriptor)
    val methodWithoutLambdaNumber = DexMethod(TypeDescriptor("Lcom/example/Foo$\$Lambda;"), "bar", emptyList(), barDescriptor)
    assertThat(method.withoutSyntheticSuffix()).isEqualTo(methodWithoutLambdaNumber)
  }
}

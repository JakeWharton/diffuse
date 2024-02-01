package com.jakewharton.diffuse

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.diffuse.format.Field
import com.jakewharton.diffuse.format.Method
import com.jakewharton.diffuse.format.TypeDescriptor
import org.junit.Test

class DexMembersTest {
  private val fooDescriptor = TypeDescriptor("Lcom/example/Foo;")
  private val barDescriptor = TypeDescriptor("Lcom/example/Bar;")

  @Test fun fieldTypeLambdaNumberRemoved() {
    val field = Field(TypeDescriptor("Lcom/example/Foo$\$Lambda$26;"), "bar", barDescriptor)
    val fieldWithoutLambdaNumber = Field(TypeDescriptor("Lcom/example/Foo$\$Lambda;"), "bar", barDescriptor)
    assertThat(field.withoutSyntheticSuffix()).isEqualTo(fieldWithoutLambdaNumber)
  }

  @Test fun methodNameSyntheticNumberRemoved() {
    val method = Method(fooDescriptor, "access$000", emptyList(), barDescriptor)
    val methodWithoutSyntheticNumber = Method(fooDescriptor, "access", emptyList(), barDescriptor)
    assertThat(method.withoutSyntheticSuffix()).isEqualTo(methodWithoutSyntheticNumber)
  }

  @Test fun methodNameLambdaNumberRemoved() {
    val method = Method(fooDescriptor, "lambda\$refreshSessionToken$14\$MainActivity", emptyList(), barDescriptor)
    val methodWithoutLambdaNumber = Method(fooDescriptor, "lambda\$refreshSessionToken\$MainActivity", emptyList(), barDescriptor)
    assertThat(method.withoutSyntheticSuffix()).isEqualTo(methodWithoutLambdaNumber)
  }

  @Test fun methodTypeLambdaNumberRemoved() {
    val method = Method(TypeDescriptor("Lcom/example/Foo$\$Lambda$26;"), "bar", emptyList(), barDescriptor)
    val methodWithoutLambdaNumber = Method(TypeDescriptor("Lcom/example/Foo$\$Lambda;"), "bar", emptyList(), barDescriptor)
    assertThat(method.withoutSyntheticSuffix()).isEqualTo(methodWithoutLambdaNumber)
  }
}

package com.jakewharton.diffuse.format

import assertk.assertThat
import assertk.assertions.containsOnly
import assertk.assertions.isEqualTo
import com.jakewharton.diffuse.format.Class.Companion.toClass
import java.util.function.Function
import org.junit.Test

@Suppress("SpellCheckingInspection") // Suppress `Lcom/**` warnings in type descriptors.
class ClassTest {
  @Test
  fun testClassParsing() {
    val input = this::class.java.requireResource($$"ClassTest$Dummy.class").asInput()
    val clazz = input.toClass()
    val type = TypeDescriptor($$"Lcom/jakewharton/diffuse/format/ClassTest$Dummy;")

    assertThat(clazz.descriptor).isEqualTo(type)
    assertThat(clazz.bytecodeVersion).isEqualTo(55) // Reflects the JVM target 11.

    val initMethod = Method(type, "<init>", emptyList(), TypeDescriptor("V"))
    val stringArrayDescriptor = TypeDescriptor("[Ljava/lang/String;")
    val getStringArrayMethod = Method(type, "getStringArray", emptyList(), stringArrayDescriptor)
    val setStringArrayMethod =
      Method(type, "setStringArray", listOf(stringArrayDescriptor), TypeDescriptor("V"))
    val useArrayMethod = Method(type, "useArray", emptyList(), TypeDescriptor("V"))
    val useLambdaMethod = Method(type, "useLambda", emptyList(), TypeDescriptor("V"))
    val lambdaMethod =
      Method(
        type,
        $$"useLambda$lambda$0",
        listOf(TypeDescriptor("Ljava/lang/String;")),
        TypeDescriptor("Ljava/lang/String;"),
      )
    val stringArrayField = Field(type, "stringArray", stringArrayDescriptor)

    assertThat(clazz.declaredMembers)
      .containsOnly(
        initMethod,
        getStringArrayMethod,
        setStringArrayMethod,
        useArrayMethod,
        useLambdaMethod,
        lambdaMethod,
        stringArrayField,
      )

    val objType = TypeDescriptor("Ljava/lang/Object;")
    val objInit = Method(objType, "<init>", emptyList(), TypeDescriptor("V"))
    val objClone = Method(objType, "clone", emptyList(), objType)
    val objToString = Method(objType, "toString", emptyList(), TypeDescriptor("Ljava/lang/String;"))
    val lambdaMetafactoryMethod =
      Method(
        TypeDescriptor("Ljava/lang/invoke/LambdaMetafactory;"),
        "metafactory",
        listOf(
          TypeDescriptor($$"Ljava/lang/invoke/MethodHandles$Lookup;"),
          TypeDescriptor("Ljava/lang/String;"),
          TypeDescriptor("Ljava/lang/invoke/MethodType;"),
          TypeDescriptor("Ljava/lang/invoke/MethodType;"),
          TypeDescriptor("Ljava/lang/invoke/MethodHandle;"),
          TypeDescriptor("Ljava/lang/invoke/MethodType;"),
        ),
        TypeDescriptor("Ljava/lang/invoke/CallSite;"),
      )
    val functionApply =
      Method(TypeDescriptor("Ljava/util/function/Function;"), "apply", listOf(objType), objType)
    val intrinsicsType = TypeDescriptor("Lkotlin/jvm/internal/Intrinsics;")
    val intrinsicsCheckNotNull =
      Method(intrinsicsType, "checkNotNull", listOf(objType), TypeDescriptor("V"))
    val intrinsicsCheckNotNullParameter =
      Method(
        intrinsicsType,
        "checkNotNullParameter",
        listOf(objType, TypeDescriptor("Ljava/lang/String;")),
        TypeDescriptor("V"),
      )
    val stringsKtType = TypeDescriptor("Lkotlin/text/StringsKt;")
    val trimMethod =
      Method(
        stringsKtType,
        "trim",
        listOf(TypeDescriptor("Ljava/lang/CharSequence;")),
        TypeDescriptor("Ljava/lang/CharSequence;"),
      )
    val arrayToString =
      Method(
        TypeDescriptor("[Ljava/lang/Object;"),
        "toString",
        emptyList(),
        TypeDescriptor("Ljava/lang/String;"),
      )

    assertThat(clazz.referencedMembers)
      .containsOnly(
        lambdaMethod,
        stringArrayField,
        objInit,
        objClone,
        objToString,
        lambdaMetafactoryMethod,
        functionApply,
        intrinsicsCheckNotNull,
        intrinsicsCheckNotNullParameter,
        trimMethod,
        arrayToString,
      )
  }

  @Suppress("unused") // Used for parsing.
  class Dummy {
    var stringArray: Array<String> = emptyArray()

    fun useArray() {
      val arr = stringArray
      arr.toString()
      arr.clone()
    }

    fun useLambda() {
      val func = Function<String, String> { it.trim() }
      func.apply(" useLambda ")
    }
  }
}

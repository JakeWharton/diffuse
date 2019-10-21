package com.jakewharton.dex

import com.google.common.io.Resources
import com.google.common.truth.Truth.assertThat
import com.jakewharton.dex.DexParser.Companion.toDexParser
import com.jakewharton.dex.DexParser.Desugaring
import com.jakewharton.diffuse.Field
import com.jakewharton.diffuse.Member
import com.jakewharton.diffuse.Method
import java.nio.file.Paths
import org.junit.Test

class DexParserTest {
  @Test fun types() {
    val dexParser = loadResource("types.dex").toDexParser()
    assertThat(dexParser.dexCount()).isEqualTo(1)
    assertThat(dexParser.listMembers().map(Member::toString)).containsExactly(
        "Types <init>()",
        "Types returnsBoolean() → boolean",
        "Types returnsRunnable() → Runnable",
        "Types returnsString() → String",
        "Types test(boolean)",
        "Types test(byte)",
        "Types test(char)",
        "Types test(double)",
        "Types test(float)",
        "Types test(int)",
        "Types test(String)",
        "Types test(String[])",
        "Types test(long)",
        "Types test(short)",
        "Types valueBoolean: boolean",
        "Types valueByte: byte",
        "Types valueChar: char",
        "Types valueDouble: double",
        "Types valueFloat: float",
        "Types valueInt: int",
        "Types valueLong: long",
        "Types valueShort: short",
        "Types valueString: String",
        "Types valueStringArray: String[]",
        "java.io.PrintStream println(String)",
        "java.lang.Object <init>()",
        "java.lang.System out: PrintStream"
    ).inOrder()
    assertThat(dexParser.declaredMembers().map(Member::toString)).containsExactly(
        "Types <init>()",
        "Types returnsBoolean() → boolean",
        "Types returnsRunnable() → Runnable",
        "Types returnsString() → String",
        "Types test(boolean)",
        "Types test(byte)",
        "Types test(char)",
        "Types test(double)",
        "Types test(float)",
        "Types test(int)",
        "Types test(String)",
        "Types test(String[])",
        "Types test(long)",
        "Types test(short)",
        "Types valueBoolean: boolean",
        "Types valueByte: byte",
        "Types valueChar: char",
        "Types valueDouble: double",
        "Types valueFloat: float",
        "Types valueInt: int",
        "Types valueLong: long",
        "Types valueShort: short",
        "Types valueString: String",
        "Types valueStringArray: String[]"
    ).inOrder()
    assertThat(dexParser.referencedMembers().map(Member::toString)).containsExactly(
        "java.io.PrintStream println(String)",
        "java.lang.Object <init>()",
        "java.lang.System out: PrintStream"
    ).inOrder()
    assertThat(dexParser.listMethods().map(Method::toString)).containsExactly(
        "Types <init>()",
        "Types returnsBoolean() → boolean",
        "Types returnsRunnable() → Runnable",
        "Types returnsString() → String",
        "Types test(boolean)",
        "Types test(byte)",
        "Types test(char)",
        "Types test(double)",
        "Types test(float)",
        "Types test(int)",
        "Types test(String)",
        "Types test(String[])",
        "Types test(long)",
        "Types test(short)",
        "java.io.PrintStream println(String)",
        "java.lang.Object <init>()"
    ).inOrder()
    assertThat(dexParser.declaredMethods().map(Method::toString)).containsExactly(
        "Types <init>()",
        "Types returnsBoolean() → boolean",
        "Types returnsRunnable() → Runnable",
        "Types returnsString() → String",
        "Types test(boolean)",
        "Types test(byte)",
        "Types test(char)",
        "Types test(double)",
        "Types test(float)",
        "Types test(int)",
        "Types test(String)",
        "Types test(String[])",
        "Types test(long)",
        "Types test(short)"
    ).inOrder()
    assertThat(dexParser.referencedMethods().map(Method::toString)).containsExactly(
        "java.io.PrintStream println(String)",
        "java.lang.Object <init>()"
    ).inOrder()
    assertThat(dexParser.listFields().map(Field::toString)).containsExactly(
        "Types valueBoolean: boolean",
        "Types valueByte: byte",
        "Types valueChar: char",
        "Types valueDouble: double",
        "Types valueFloat: float",
        "Types valueInt: int",
        "Types valueLong: long",
        "Types valueShort: short",
        "Types valueString: String",
        "Types valueStringArray: String[]",
        "java.lang.System out: PrintStream"
    ).inOrder()
    assertThat(dexParser.declaredFields().map(Field::toString)).containsExactly(
        "Types valueBoolean: boolean",
        "Types valueByte: byte",
        "Types valueChar: char",
        "Types valueDouble: double",
        "Types valueFloat: float",
        "Types valueInt: int",
        "Types valueLong: long",
        "Types valueShort: short",
        "Types valueString: String",
        "Types valueStringArray: String[]"
    ).inOrder()
    assertThat(dexParser.referencedFields().map(Field::toString)).containsExactly(
        "java.lang.System out: PrintStream"
    ).inOrder()
  }

  @Test fun paramsJoined() {
    val dexParser = loadResource("params_joined.dex").toDexParser()
    assertThat(dexParser.listMembers().map(Member::toString)).containsExactly(
        "Params <init>()",
        "Params test(String, String, String, String)",
        "java.lang.Object <init>()"
    ).inOrder()
  }

  @Test fun visibilities() {
    val dexParser = loadResource("visibilities.dex").toDexParser()
    assertThat(dexParser.listMembers().map(Member::toString)).containsExactly(
        "Visibilities <init>()",
        "Visibilities test1()",
        "Visibilities test2()",
        "Visibilities test3()",
        "Visibilities test4()",
        "Visibilities test1: String",
        "Visibilities test2: String",
        "Visibilities test3: String",
        "Visibilities test4: String",
        "java.lang.Object <init>()"
    ).inOrder()
  }

  @Test fun multipleDexFiles() {
    val types = loadResource("types.dex")
    val visibilities = loadResource("visibilities.dex")
    val dexParser = listOf(types, visibilities).toDexParser()
    assertThat(dexParser.listMembers().map(Member::toString)).containsExactly(
        "Types <init>()",
        "Types returnsBoolean() → boolean",
        "Types returnsRunnable() → Runnable",
        "Types returnsString() → String",
        "Types test(boolean)",
        "Types test(byte)",
        "Types test(char)",
        "Types test(double)",
        "Types test(float)",
        "Types test(int)",
        "Types test(String)",
        "Types test(String[])",
        "Types test(long)",
        "Types test(short)",
        "Types valueBoolean: boolean",
        "Types valueByte: byte",
        "Types valueChar: char",
        "Types valueDouble: double",
        "Types valueFloat: float",
        "Types valueInt: int",
        "Types valueLong: long",
        "Types valueShort: short",
        "Types valueString: String",
        "Types valueStringArray: String[]",
        "Visibilities <init>()",
        "Visibilities test1()",
        "Visibilities test2()",
        "Visibilities test3()",
        "Visibilities test4()",
        "Visibilities test1: String",
        "Visibilities test2: String",
        "Visibilities test3: String",
        "Visibilities test4: String",
        "java.io.PrintStream println(String)",
        "java.lang.Object <init>()",
        "java.lang.System out: PrintStream"
    ).inOrder()
  }

  @Test fun apk() {
    val dexParser = loadResource("one.apk").toDexParser()
    assertThat(dexParser.listMembers().map(Member::toString)).containsExactly(
        "Types <init>()",
        "Types returnsBoolean() → boolean",
        "Types returnsRunnable() → Runnable",
        "Types returnsString() → String",
        "Types test(boolean)",
        "Types test(byte)",
        "Types test(char)",
        "Types test(double)",
        "Types test(float)",
        "Types test(int)",
        "Types test(String)",
        "Types test(String[])",
        "Types test(long)",
        "Types test(short)",
        "Types valueBoolean: boolean",
        "Types valueByte: byte",
        "Types valueChar: char",
        "Types valueDouble: double",
        "Types valueFloat: float",
        "Types valueInt: int",
        "Types valueLong: long",
        "Types valueShort: short",
        "Types valueString: String",
        "Types valueStringArray: String[]",
        "java.io.PrintStream println(String)",
        "java.lang.Object <init>()",
        "java.lang.System out: PrintStream"
    ).inOrder()
  }

  @Test fun apkMultipleDex() {
    val dexParser = loadResource("three.apk").toDexParser()
    assertThat(dexParser.listMembers().map(Member::toString)).containsExactly(
        "Params <init>()",
        "Params test(String, String, String, String)",
        "Types <init>()",
        "Types returnsBoolean() → boolean",
        "Types returnsRunnable() → Runnable",
        "Types returnsString() → String",
        "Types test(boolean)",
        "Types test(byte)",
        "Types test(char)",
        "Types test(double)",
        "Types test(float)",
        "Types test(int)",
        "Types test(String)",
        "Types test(String[])",
        "Types test(long)",
        "Types test(short)",
        "Types valueBoolean: boolean",
        "Types valueByte: byte",
        "Types valueChar: char",
        "Types valueDouble: double",
        "Types valueFloat: float",
        "Types valueInt: int",
        "Types valueLong: long",
        "Types valueShort: short",
        "Types valueString: String",
        "Types valueStringArray: String[]",
        "Visibilities <init>()",
        "Visibilities test1()",
        "Visibilities test2()",
        "Visibilities test3()",
        "Visibilities test4()",
        "Visibilities test1: String",
        "Visibilities test2: String",
        "Visibilities test3: String",
        "Visibilities test4: String",
        "java.io.PrintStream println(String)",
        "java.lang.Object <init>()",
        "java.lang.System out: PrintStream"
    ).inOrder()
  }

  @Test fun classFile() {
    val dexParser = loadResource("Visibilities.class").toDexParser()
    assertThat(dexParser.listMembers().map(Member::toString)).containsExactly(
        "Visibilities <init>()",
        "Visibilities test1()",
        "Visibilities test2()",
        "Visibilities test3()",
        "Visibilities test4()",
        "Visibilities test1: String",
        "Visibilities test2: String",
        "Visibilities test3: String",
        "Visibilities test4: String",
        "java.lang.Object <init>()"
    ).inOrder()
  }

  @Test fun multipleClassFiles() {
    val params = loadResource("Types.class")
    val visibilities = loadResource("Visibilities.class")
    val dexParser = listOf(params, visibilities).toDexParser()
    assertThat(dexParser.listMembers().map(Member::toString)).containsExactly(
        "Types <init>()",
        "Types returnsBoolean() → boolean",
        "Types returnsRunnable() → Runnable",
        "Types returnsString() → String",
        "Types test(boolean)",
        "Types test(byte)",
        "Types test(char)",
        "Types test(double)",
        "Types test(float)",
        "Types test(int)",
        "Types test(String)",
        "Types test(String[])",
        "Types test(long)",
        "Types test(short)",
        "Types valueBoolean: boolean",
        "Types valueByte: byte",
        "Types valueChar: char",
        "Types valueDouble: double",
        "Types valueFloat: float",
        "Types valueInt: int",
        "Types valueLong: long",
        "Types valueShort: short",
        "Types valueString: String",
        "Types valueStringArray: String[]",
        "Visibilities <init>()",
        "Visibilities test1()",
        "Visibilities test2()",
        "Visibilities test3()",
        "Visibilities test4()",
        "Visibilities test1: String",
        "Visibilities test2: String",
        "Visibilities test3: String",
        "Visibilities test4: String",
        "java.io.PrintStream println(String)",
        "java.lang.Object <init>()",
        "java.lang.System out: PrintStream"
    ).inOrder()
  }

  @Test fun jarFile() {
    val dexParser = loadResource("types.jar").toDexParser()
    assertThat(dexParser.listMembers().map(Member::toString)).containsExactly(
        "Types <init>()",
        "Types returnsBoolean() → boolean",
        "Types returnsRunnable() → Runnable",
        "Types returnsString() → String",
        "Types test(boolean)",
        "Types test(byte)",
        "Types test(char)",
        "Types test(double)",
        "Types test(float)",
        "Types test(int)",
        "Types test(String)",
        "Types test(String[])",
        "Types test(long)",
        "Types test(short)",
        "Types valueBoolean: boolean",
        "Types valueByte: byte",
        "Types valueChar: char",
        "Types valueDouble: double",
        "Types valueFloat: float",
        "Types valueInt: int",
        "Types valueLong: long",
        "Types valueShort: short",
        "Types valueString: String",
        "Types valueStringArray: String[]",
        "java.io.PrintStream println(String)",
        "java.lang.Object <init>()",
        "java.lang.System out: PrintStream"
    ).inOrder()
  }

  @Test fun multipleJarFiles() {
    val types = loadResource("types.jar")
    val visibilities = loadResource("visibilities.jar")
    val dexParser = listOf(types, visibilities).toDexParser()
    assertThat(dexParser.listMembers().map(Member::toString)).containsExactly(
        "Types <init>()",
        "Types returnsBoolean() → boolean",
        "Types returnsRunnable() → Runnable",
        "Types returnsString() → String",
        "Types test(boolean)",
        "Types test(byte)",
        "Types test(char)",
        "Types test(double)",
        "Types test(float)",
        "Types test(int)",
        "Types test(String)",
        "Types test(String[])",
        "Types test(long)",
        "Types test(short)",
        "Types valueBoolean: boolean",
        "Types valueByte: byte",
        "Types valueChar: char",
        "Types valueDouble: double",
        "Types valueFloat: float",
        "Types valueInt: int",
        "Types valueLong: long",
        "Types valueShort: short",
        "Types valueString: String",
        "Types valueStringArray: String[]",
        "Visibilities <init>()",
        "Visibilities test1()",
        "Visibilities test2()",
        "Visibilities test3()",
        "Visibilities test4()",
        "Visibilities test1: String",
        "Visibilities test2: String",
        "Visibilities test3: String",
        "Visibilities test4: String",
        "java.io.PrintStream println(String)",
        "java.lang.Object <init>()",
        "java.lang.System out: PrintStream"
    ).inOrder()
  }

  @Test fun jarMultipleClasses() {
    val dexParser = loadResource("three.jar").toDexParser()
    assertThat(dexParser.listMembers().map(Member::toString)).containsExactly(
        "Params <init>()",
        "Params test(String, String, String, String)",
        "Types <init>()",
        "Types returnsBoolean() → boolean",
        "Types returnsRunnable() → Runnable",
        "Types returnsString() → String",
        "Types test(boolean)",
        "Types test(byte)",
        "Types test(char)",
        "Types test(double)",
        "Types test(float)",
        "Types test(int)",
        "Types test(String)",
        "Types test(String[])",
        "Types test(long)",
        "Types test(short)",
        "Types valueBoolean: boolean",
        "Types valueByte: byte",
        "Types valueChar: char",
        "Types valueDouble: double",
        "Types valueFloat: float",
        "Types valueInt: int",
        "Types valueLong: long",
        "Types valueShort: short",
        "Types valueString: String",
        "Types valueStringArray: String[]",
        "Visibilities <init>()",
        "Visibilities test1()",
        "Visibilities test2()",
        "Visibilities test3()",
        "Visibilities test4()",
        "Visibilities test1: String",
        "Visibilities test2: String",
        "Visibilities test3: String",
        "Visibilities test4: String",
        "java.io.PrintStream println(String)",
        "java.lang.Object <init>()",
        "java.lang.System out: PrintStream"
    ).inOrder()
  }

  @Test fun dexBytes() {
    val dexParser = loadResource("params_joined.dex").readBytes().toDexParser()
    assertThat(dexParser.listMembers().map(Member::toString)).containsExactly(
        "Params <init>()",
        "Params test(String, String, String, String)",
        "java.lang.Object <init>()"
    ).inOrder()
  }

  @Test fun apkBytes() {
    val dexParser = loadResource("one.apk").readBytes().toDexParser()
    assertThat(dexParser.listMembers().map { it.toString() }).containsExactly(
        "Types <init>()",
        "Types returnsBoolean() → boolean",
        "Types returnsRunnable() → Runnable",
        "Types returnsString() → String",
        "Types test(boolean)",
        "Types test(byte)",
        "Types test(char)",
        "Types test(double)",
        "Types test(float)",
        "Types test(int)",
        "Types test(String)",
        "Types test(String[])",
        "Types test(long)",
        "Types test(short)",
        "Types valueBoolean: boolean",
        "Types valueByte: byte",
        "Types valueChar: char",
        "Types valueDouble: double",
        "Types valueFloat: float",
        "Types valueInt: int",
        "Types valueLong: long",
        "Types valueShort: short",
        "Types valueString: String",
        "Types valueStringArray: String[]",
        "java.io.PrintStream println(String)",
        "java.lang.Object <init>()",
        "java.lang.System out: PrintStream"
    ).inOrder()
  }

  @Test fun classBytes() {
    val dexParser = loadResource("Params.class").readBytes().toDexParser()
    assertThat(dexParser.listMembers().map(Member::toString)).containsExactly(
        "Params <init>()",
        "Params test(String, String, String, String)",
        "java.lang.Object <init>()"
    ).inOrder()
  }

  @Test fun jarBytes() {
    val dexParser = loadResource("params_joined.jar").readBytes().toDexParser()
    assertThat(dexParser.listMembers().map(Member::toString)).containsExactly(
        "Params <init>()",
        "Params test(String, String, String, String)",
        "java.lang.Object <init>()"
    ).inOrder()
  }

  @Test fun aarExtractsJars() {
    val dexParser = loadResource("three.aar").toDexParser()
    assertThat(dexParser.listMembers().map(Member::toString)).containsExactly(
        "Params <init>()",
        "Params test(String, String, String, String)",
        "Types <init>()",
        "Types returnsBoolean() → boolean",
        "Types returnsRunnable() → Runnable",
        "Types returnsString() → String",
        "Types test(boolean)",
        "Types test(byte)",
        "Types test(char)",
        "Types test(double)",
        "Types test(float)",
        "Types test(int)",
        "Types test(String)",
        "Types test(String[])",
        "Types test(long)",
        "Types test(short)",
        "Types valueBoolean: boolean",
        "Types valueByte: byte",
        "Types valueChar: char",
        "Types valueDouble: double",
        "Types valueFloat: float",
        "Types valueInt: int",
        "Types valueLong: long",
        "Types valueShort: short",
        "Types valueString: String",
        "Types valueStringArray: String[]",
        "Visibilities <init>()",
        "Visibilities test1()",
        "Visibilities test2()",
        "Visibilities test3()",
        "Visibilities test4()",
        "Visibilities test1: String",
        "Visibilities test2: String",
        "Visibilities test3: String",
        "Visibilities test4: String",
        "java.io.PrintStream println(String)",
        "java.lang.Object <init>()",
        "java.lang.System out: PrintStream"
    ).inOrder()
  }

  @Test fun desugaring() {
    val dexParser = loadResource("Desugaring.class").toDexParser()
    assertThat(dexParser.listMembers().map(Member::toString)).containsExactly(
        "Desugaring <init>(Runnable)",
        "Desugaring lambda\$main$0()",
        "Desugaring main(String[])",
        "Desugaring run()",
        "Desugaring delegate: Runnable",
        "java.io.PrintStream println(String)",
        "java.lang.Object <init>()",
        "java.lang.Runnable run()",
        "java.lang.System out: PrintStream",
        "java.lang.invoke.LambdaMetafactory metafactory(MethodHandles\$Lookup, String, MethodType, MethodType, MethodHandle, MethodType) → CallSite",
        "java.util.Objects requireNonNull(Object) → Object"
    ).inOrder()

    val androidApi2Jar = loadResource("android-api-2.jar")
    val api24Parser = dexParser.withDesugaring(Desugaring(24, listOf(androidApi2Jar)))
    assertThat(api24Parser.listMembers().map(Member::toString)).containsExactly(
        "-$\$Lambda\$Desugaring\$6rYBk5woQz1Eg9tpH9D8Lr-uUbQ <clinit>()",
        "-$\$Lambda\$Desugaring\$6rYBk5woQz1Eg9tpH9D8Lr-uUbQ <init>()",
        "-$\$Lambda\$Desugaring\$6rYBk5woQz1Eg9tpH9D8Lr-uUbQ run()",
        "-$\$Lambda\$Desugaring\$6rYBk5woQz1Eg9tpH9D8Lr-uUbQ INSTANCE: -$\$Lambda\$Desugaring\$6rYBk5woQz1Eg9tpH9D8Lr-uUbQ",
        "Desugaring <init>(Runnable)",
        "Desugaring lambda\$main$0()",
        "Desugaring main(String[])",
        "Desugaring run()",
        "Desugaring delegate: Runnable",
        "java.io.PrintStream println(String)",
        "java.lang.Object <init>()",
        "java.lang.Runnable run()",
        "java.lang.System out: PrintStream",
        "java.util.Objects requireNonNull(Object) → Object"
    ).inOrder()

    val api14Parser = dexParser.withDesugaring(Desugaring(14, listOf(androidApi2Jar)))
    assertThat(api14Parser.listMembers().map(Member::toString)).containsExactly(
        "-$\$Lambda\$Desugaring\$6rYBk5woQz1Eg9tpH9D8Lr-uUbQ <clinit>()",
        "-$\$Lambda\$Desugaring\$6rYBk5woQz1Eg9tpH9D8Lr-uUbQ <init>()",
        "-$\$Lambda\$Desugaring\$6rYBk5woQz1Eg9tpH9D8Lr-uUbQ run()",
        "-$\$Lambda\$Desugaring\$6rYBk5woQz1Eg9tpH9D8Lr-uUbQ INSTANCE: -$\$Lambda\$Desugaring\$6rYBk5woQz1Eg9tpH9D8Lr-uUbQ",
        "Desugaring <init>(Runnable)",
        "Desugaring lambda\$main$0()",
        "Desugaring main(String[])",
        "Desugaring run()",
        "Desugaring delegate: Runnable",
        "java.io.PrintStream println(String)",
        "java.lang.Object <init>()",
        "java.lang.Object getClass() → Class",
        "java.lang.Runnable run()",
        "java.lang.System out: PrintStream"
    ).inOrder()
  }

  @Test fun emptyJar() {
    val dexParser = loadResource("empty.jar").readBytes().toDexParser()
    assertThat(dexParser.listMembers().map(Member::toString)).isEmpty()
  }

  private fun loadResource(resourceName: String) =
      Paths.get(Resources.getResource(resourceName).file)
}

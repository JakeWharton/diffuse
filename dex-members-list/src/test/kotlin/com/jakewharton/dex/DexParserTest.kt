package com.jakewharton.dex

import com.google.common.io.Resources
import com.google.common.truth.Truth.assertThat
import com.jakewharton.dex.DexParser.Companion.toDexParser
import org.junit.Test
import java.io.File

class DexParserTest {
  @Test fun types() {
    val dexParser = File(Resources.getResource("types.dex").file).toDexParser()
    assertThat(dexParser.dexCount()).isEqualTo(1)
    assertThat(dexParser.list().map(DexMember::toString)).containsExactly(
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
    assertThat(dexParser.listMethods().map(DexMethod::toString)).containsExactly(
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
    assertThat(dexParser.listFields().map(DexField::toString)).containsExactly(
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
  }

  @Test fun paramsJoined() {
    val dexParser = File(Resources.getResource("params_joined.dex").file).toDexParser()
    assertThat(dexParser.list().map(DexMember::toString)).containsExactly(
        "Params <init>()",
        "Params test(String, String, String, String)",
        "java.lang.Object <init>()"
    ).inOrder()
  }

  @Test fun visibilities() {
    val dexParser = File(Resources.getResource("visibilities.dex").file).toDexParser()
    assertThat(dexParser.list().map(DexMember::toString)).containsExactly(
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
    val types = File(Resources.getResource("types.dex").file)
    val visibilities = File(Resources.getResource("visibilities.dex").file)
    val dexParser = listOf(types, visibilities).toDexParser()
    assertThat(dexParser.list().map(DexMember::toString)).containsExactly(
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
    val dexParser = File(Resources.getResource("one.apk").file).toDexParser()
    assertThat(dexParser.list().map(DexMember::toString)).containsExactly(
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
    val dexParser = File(Resources.getResource("three.apk").file).toDexParser()
    assertThat(dexParser.list().map(DexMember::toString)).containsExactly(
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
    val dexParser = File(Resources.getResource("Visibilities.class").file).toDexParser()
    assertThat(dexParser.list().map(DexMember::toString)).containsExactly(
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
    val params = File(Resources.getResource("Types.class").file)
    val visibilities = File(Resources.getResource("Visibilities.class").file)
    val dexParser = listOf(params, visibilities).toDexParser()
    assertThat(dexParser.list().map(DexMember::toString)).containsExactly(
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
    val dexParser = File(Resources.getResource("types.jar").file).toDexParser()
    assertThat(dexParser.list().map(DexMember::toString)).containsExactly(
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
    val types = File(Resources.getResource("types.jar").file)
    val visibilities = File(Resources.getResource("visibilities.jar").file)
    val dexParser = listOf(types, visibilities).toDexParser()
    assertThat(dexParser.list().map(DexMember::toString)).containsExactly(
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
    val dexParser = File(Resources.getResource("three.jar").file).toDexParser()
    assertThat(dexParser.list().map(DexMember::toString)).containsExactly(
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
    val dexParser = Resources.toByteArray(Resources.getResource("params_joined.dex")).toDexParser()
    assertThat(dexParser.list().map(DexMember::toString)).containsExactly(
        "Params <init>()",
        "Params test(String, String, String, String)",
        "java.lang.Object <init>()"
    ).inOrder()
  }

  @Test fun apkBytes() {
    val dexParser = Resources.toByteArray(Resources.getResource("one.apk")).toDexParser()
    assertThat(dexParser.list().map { it.toString() }).containsExactly(
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
    val dexParser = Resources.toByteArray(Resources.getResource("Params.class")).toDexParser()
    assertThat(dexParser.list().map(DexMember::toString)).containsExactly(
        "Params <init>()",
        "Params test(String, String, String, String)",
        "java.lang.Object <init>()"
    ).inOrder()
  }

  @Test fun jarBytes() {
    val dexParser = Resources.toByteArray(Resources.getResource("params_joined.jar")).toDexParser()
    assertThat(dexParser.list().map(DexMember::toString)).containsExactly(
        "Params <init>()",
        "Params test(String, String, String, String)",
        "java.lang.Object <init>()"
    ).inOrder()
  }

  @Test fun aarExtractsJars() {
    val dexParser = File(Resources.getResource("three.aar").file).toDexParser()
    assertThat(dexParser.list().map(DexMember::toString)).containsExactly(
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
}

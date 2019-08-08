package com.jakewharton.dex

import com.google.common.io.Resources
import com.google.common.truth.Truth.assertThat
import com.jakewharton.dex.DexParser.Companion.toDexParser
import org.junit.Test
import java.io.File

class DexMethodsTest {
  @Test fun types() {
    val types = File(Resources.getResource("types.dex").file)
    val methods = types.toDexParser()
        .listMethods()
        .map { it.render() }
    assertThat(methods).containsExactly(
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
        "java.lang.Object <init>()"
    ).inOrder()
  }

  @Test fun paramsJoined() {
    val params = File(Resources.getResource("params_joined.dex").file)
    val methods = params.toDexParser()
        .listMethods()
        .map { it.render() }
    assertThat(methods).containsExactly(
        "Params <init>()",
        "Params test(String, String, String, String)",
        "java.lang.Object <init>()"
    ).inOrder()
  }

  @Test fun visibilities() {
    val visibilities = File(Resources.getResource("visibilities.dex").file)
    val methods = visibilities.toDexParser()
        .listMethods()
        .map { it.render() }
    assertThat(methods).containsExactly(
        "Visibilities <init>()",
        "Visibilities test1()",
        "Visibilities test2()",
        "Visibilities test3()",
        "Visibilities test4()",
        "java.lang.Object <init>()"
    ).inOrder()
  }

  @Test fun multipleDexFiles() {
    val params = File(Resources.getResource("params_joined.dex").file)
    val visibilities = File(Resources.getResource("visibilities.dex").file)
    val methods = listOf(params, visibilities).toDexParser()
        .listMethods()
        .map { it.render() }
    assertThat(methods).containsExactly(
        "Params <init>()",
        "Params test(String, String, String, String)",
        "Visibilities <init>()",
        "Visibilities test1()",
        "Visibilities test2()",
        "Visibilities test3()",
        "Visibilities test4()",
        "java.lang.Object <init>()"
    ).inOrder()
  }

  @Test fun apk() {
    val one = File(Resources.getResource("one.apk").file)
    val methods = one.toDexParser()
        .listMethods()
        .map { it.render() }
    assertThat(methods).containsExactly(
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
        "java.lang.Object <init>()"
    ).inOrder()
  }

  @Test fun apkMultipleDex() {
    val three = File(Resources.getResource("three.apk").file)
    val methods = three.toDexParser()
        .listMethods()
        .map { it.render() }
    assertThat(methods).containsExactly(
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
        "Visibilities <init>()",
        "Visibilities test1()",
        "Visibilities test2()",
        "Visibilities test3()",
        "Visibilities test4()",
        "java.lang.Object <init>()"
    ).inOrder()
  }

  @Test fun classFile() {
    val params = File(Resources.getResource("Params.class").file)
    val methods = params.toDexParser()
        .listMethods()
        .map { it.render() }
    assertThat(methods).containsExactly(
        "Params <init>()",
        "Params test(String, String, String, String)",
        "java.lang.Object <init>()"
    ).inOrder()
  }

  @Test fun multipleClassFiles() {
    val params = File(Resources.getResource("Params.class").file)
    val visibilities = File(Resources.getResource("Visibilities.class").file)
    val methods = listOf(params, visibilities).toDexParser()
        .listMethods()
        .map { it.render() }
    assertThat(methods).containsExactly(
        "Params <init>()",
        "Params test(String, String, String, String)",
        "Visibilities <init>()",
        "Visibilities test1()",
        "Visibilities test2()",
        "Visibilities test3()",
        "Visibilities test4()",
        "java.lang.Object <init>()"
    ).inOrder()
  }

  @Test fun jarFile() {
    val params = File(Resources.getResource("params_joined.jar").file)
    val methods = params.toDexParser()
        .listMethods()
        .map { it.render() }
    assertThat(methods).containsExactly(
        "Params <init>()",
        "Params test(String, String, String, String)",
        "java.lang.Object <init>()"
    ).inOrder()
  }

  @Test fun multipleJarFiles() {
    val params = File(Resources.getResource("params_joined.jar").file)
    val visibilities = File(Resources.getResource("visibilities.jar").file)
    val methods = listOf(params, visibilities).toDexParser()
        .listMethods()
        .map { it.render() }
    assertThat(methods).containsExactly(
        "Params <init>()",
        "Params test(String, String, String, String)",
        "Visibilities <init>()",
        "Visibilities test1()",
        "Visibilities test2()",
        "Visibilities test3()",
        "Visibilities test4()",
        "java.lang.Object <init>()"
    ).inOrder()
  }

  @Test fun jarMultipleClasses() {
    val three = File(Resources.getResource("three.jar").file)
    val methods = three.toDexParser()
        .listMethods()
        .map { it.render() }
    assertThat(methods).containsExactly(
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
        "Visibilities <init>()",
        "Visibilities test1()",
        "Visibilities test2()",
        "Visibilities test3()",
        "Visibilities test4()",
        "java.lang.Object <init>()"
    ).inOrder()
  }

  @Test fun dexBytes() {
    val bytes = Resources.toByteArray(Resources.getResource("params_joined.dex"))
    val methods = bytes.toDexParser()
        .listMethods()
        .map { it.render() }
    assertThat(methods).containsExactly(
        "Params <init>()",
        "Params test(String, String, String, String)",
        "java.lang.Object <init>()"
    ).inOrder()
  }

  @Test fun apkBytes() {
    val bytes = Resources.toByteArray(Resources.getResource("one.apk"))
    val methods = bytes.toDexParser()
        .listMethods()
        .map { it.render() }
    assertThat(methods).containsExactly(
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
        "java.lang.Object <init>()"
    ).inOrder()
  }

  @Test fun classBytes() {
    val bytes = Resources.toByteArray(Resources.getResource("Params.class"))
    val methods = bytes.toDexParser()
        .listMethods()
        .map { it.render() }
    assertThat(methods).containsExactly(
        "Params <init>()",
        "Params test(String, String, String, String)",
        "java.lang.Object <init>()"
    ).inOrder()
  }

  @Test fun jarBytes() {
    val bytes = Resources.toByteArray(Resources.getResource("params_joined.jar"))
    val methods = bytes.toDexParser()
        .listMethods()
        .map { it.render() }
    assertThat(methods).containsExactly(
        "Params <init>()",
        "Params test(String, String, String, String)",
        "java.lang.Object <init>()"
    ).inOrder()
  }

  @Test fun aarExtractsJars() {
    val three = File(Resources.getResource("three.aar").file)
    val methods = three.toDexParser()
        .listMethods()
        .map { it.render() }
    assertThat(methods).containsExactly(
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
        "Visibilities <init>()",
        "Visibilities test1()",
        "Visibilities test2()",
        "Visibilities test3()",
        "Visibilities test4()",
        "java.lang.Object <init>()"
    ).inOrder()
  }

  @Test fun synthetics() {
    val types = File(Resources.getResource("synthetics.dex").file)
    val methods = types.toDexParser()
        .listMethods()
        .map { it.render() }
    assertThat(methods).containsExactly(
        "Synthetics <init>()",
        "Synthetics access$000(Synthetics) → int",
        "Synthetics access$002(Synthetics, int) → int",
        "Synthetics access$004(Synthetics) → int",
        "Synthetics access$006(Synthetics) → int",
        "Synthetics access$008(Synthetics) → int",
        "Synthetics access$010(Synthetics) → int",
        "Synthetics\$Inner <init>(Synthetics)",
        "java.io.PrintStream println(int)",
        "java.lang.Object <init>()"
    ).inOrder()
  }

  @Test fun syntheticsWithoutNumbers() {
    val types = File(Resources.getResource("synthetics.dex").file)
    val bytes = types.readBytes()
    val methods = bytes.toDexParser()
        .listMethods()
        .map { it.render(true) }
    assertThat(methods).containsExactly(
        "Synthetics <init>()",
        "Synthetics access(Synthetics) → int",
        "Synthetics access(Synthetics, int) → int",
        "Synthetics access(Synthetics) → int",
        "Synthetics access(Synthetics) → int",
        "Synthetics access(Synthetics) → int",
        "Synthetics access(Synthetics) → int",
        "Synthetics\$Inner <init>(Synthetics)",
        "java.io.PrintStream println(int)",
        "java.lang.Object <init>()"
    ).inOrder()
  }
}

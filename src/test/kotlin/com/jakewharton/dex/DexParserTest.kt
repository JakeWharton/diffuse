package com.jakewharton.dex

import com.google.common.io.Resources
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.io.File

class DexParserTest {
  @Test fun types() {
    val types = File(Resources.getResource("types.dex").file)
    val dexParser = DexParser.fromFile(types)
    val methods = dexParser
        .list()
        .map { it.toString() }
    assertThat(dexParser.dexCount()).isEqualTo(1)
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
        "java.lang.Object <init>()"
    ).inOrder()
  }
}

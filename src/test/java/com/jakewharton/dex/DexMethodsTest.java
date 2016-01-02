package com.jakewharton.dex;

import com.google.common.io.Resources;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public final class DexMethodsTest {
  @Test public void types() throws IOException {
    File types = new File(Resources.getResource("types.dex").getFile());
    List<String> methods = DexMethods.list(types);
    assertThat(methods).containsExactly(
        "Types <init>()",
        "Types test(String)",
        "Types test(String[])",
        "Types test(boolean)",
        "Types test(byte)",
        "Types test(char)",
        "Types test(double)",
        "Types test(float)",
        "Types test(int)",
        "Types test(long)",
        "Types test(short)",
        "java.lang.Object <init>()"
    );
  }

  @Test public void paramsJoined() throws IOException {
    File params = new File(Resources.getResource("params_joined.dex").getFile());
    List<String> methods = DexMethods.list(params);
    assertThat(methods).containsExactly(
        "Params <init>()",
        "Params test(String, String, String, String)",
        "java.lang.Object <init>()"
    );
  }

  @Test public void visibilities() throws IOException {
    File visibilities = new File(Resources.getResource("visibilities.dex").getFile());
    List<String> methods = DexMethods.list(visibilities);
    assertThat(methods).containsExactly(
        "Visibilities <init>()",
        "Visibilities test1()",
        "Visibilities test2()",
        "Visibilities test3()",
        "Visibilities test4()",
        "java.lang.Object <init>()"
    );
  }

  @Test public void multipleDexFiles() {
    File params = new File(Resources.getResource("params_joined.dex").getFile());
    File visibilities = new File(Resources.getResource("visibilities.dex").getFile());
    List<String> methods = DexMethods.list(params, visibilities);
    assertThat(methods).containsExactly(
        "Params <init>()",
        "Params test(String, String, String, String)",
        "Visibilities <init>()",
        "Visibilities test1()",
        "Visibilities test2()",
        "Visibilities test3()",
        "Visibilities test4()",
        "java.lang.Object <init>()",
        "java.lang.Object <init>()"
    );
  }


  @Test public void apk() throws IOException {
    File one = new File(Resources.getResource("one.apk").getFile());
    List<String> methods = DexMethods.list(one);
    assertThat(methods).containsExactly(
        "Params <init>()",
        "Params test(String, String, String, String)",
        "java.lang.Object <init>()"
    );
  }

  @Test public void apkMultipleDex() {
    File three = new File(Resources.getResource("three.apk").getFile());
    List<String> methods = DexMethods.list(three);
    assertThat(methods).containsExactly(
        "Params <init>()",
        "Params test(String, String, String, String)",
        "Types <init>()",
        "Types test(String)",
        "Types test(String[])",
        "Types test(boolean)",
        "Types test(byte)",
        "Types test(char)",
        "Types test(double)",
        "Types test(float)",
        "Types test(int)",
        "Types test(long)",
        "Types test(short)",
        "Visibilities <init>()",
        "Visibilities test1()",
        "Visibilities test2()",
        "Visibilities test3()",
        "Visibilities test4()",
        "java.lang.Object <init>()",
        "java.lang.Object <init>()",
        "java.lang.Object <init>()"
    );
  }

  @Test public void dexBytes() throws IOException {
    byte[] dex = Resources.toByteArray(Resources.getResource("params_joined.dex"));
    List<String> methods = DexMethods.list(dex);
    assertThat(methods).containsExactly(
        "Params <init>()",
        "Params test(String, String, String, String)",
        "java.lang.Object <init>()"
    );
  }

  @Test public void apkBytes() throws IOException {
    byte[] apk = Resources.toByteArray(Resources.getResource("one.apk"));
    List<String> methods = DexMethods.list(apk);
    assertThat(methods).containsExactly(
        "Params <init>()",
        "Params test(String, String, String, String)",
        "java.lang.Object <init>()"
    );
  }
}

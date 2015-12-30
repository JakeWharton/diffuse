package com.jakewharton.dexmethodlist;

import com.google.common.io.Resources;
import java.io.IOException;
import org.junit.Rule;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public final class MainTest {
  @Rule public final SystemOutCaptureRule out = new SystemOutCaptureRule();

  @Test public void singleArgument() {
    Main.main(new String[] {
        Resources.getResource("params_joined.dex").getFile()
    });
    assertThat(out.get()).isEqualTo(""
        + "Params <init>()\n"
        + "Params test(String, String, String, String)\n"
        + "java.lang.Object <init>()\n");
  }

  @Test public void multipleArguments() {
    Main.main(new String[] {
        Resources.getResource("params_joined.dex").getFile(),
        Resources.getResource("visibilities.dex").getFile()
    });
    assertThat(out.get()).isEqualTo(""
        + "Params <init>()\n"
        + "Params test(String, String, String, String)\n"
        + "Visibilities <init>()\n"
        + "Visibilities test1()\n"
        + "Visibilities test2()\n"
        + "Visibilities test3()\n"
        + "Visibilities test4()\n"
        + "java.lang.Object <init>()\n"
        + "java.lang.Object <init>()\n");
  }

  @Test public void types() throws IOException {
    System.setIn(Resources.getResource("types.dex").openStream());
    Main.main(new String[0]);
    assertThat(out.get()).isEqualTo(""
        + "Types <init>()\n"
        + "Types test(String)\n"
        + "Types test(String[])\n"
        + "Types test(boolean)\n"
        + "Types test(byte)\n"
        + "Types test(char)\n"
        + "Types test(double)\n"
        + "Types test(float)\n"
        + "Types test(int)\n"
        + "Types test(long)\n"
        + "Types test(short)\n"
        + "java.lang.Object <init>()\n");
  }

  @Test public void paramsJoined() throws IOException {
    System.setIn(Resources.getResource("params_joined.dex").openStream());
    Main.main(new String[0]);
    assertThat(out.get()).isEqualTo(""
        + "Params <init>()\n"
        + "Params test(String, String, String, String)\n"
        + "java.lang.Object <init>()\n");
  }

  @Test public void visibilities() throws IOException {
    System.setIn(Resources.getResource("visibilities.dex").openStream());
    Main.main(new String[0]);
    assertThat(out.get()).isEqualTo(""
        + "Visibilities <init>()\n"
        + "Visibilities test1()\n"
        + "Visibilities test2()\n"
        + "Visibilities test3()\n"
        + "Visibilities test4()\n"
        + "java.lang.Object <init>()\n");
  }

  @Test public void apk() throws IOException {
    System.setIn(Resources.getResource("one.apk").openStream());
    Main.main(new String[0]);
    assertThat(out.get()).isEqualTo(""
        + "Params <init>()\n"
        + "Params test(String, String, String, String)\n"
        + "java.lang.Object <init>()\n");
  }

  @Test public void apkArgumentOneDex() {
    Main.main(new String[] {
        Resources.getResource("one.apk").getFile()
    });
    assertThat(out.get()).isEqualTo(""
        + "Params <init>()\n"
        + "Params test(String, String, String, String)\n"
        + "java.lang.Object <init>()\n");
  }

  @Test public void apkArgumentThreeDex() {
    Main.main(new String[] {
        Resources.getResource("three.apk").getFile()
    });
    assertThat(out.get()).isEqualTo(""
        + "Params <init>()\n"
        + "Params test(String, String, String, String)\n"
        + "Types <init>()\n"
        + "Types test(String)\n"
        + "Types test(String[])\n"
        + "Types test(boolean)\n"
        + "Types test(byte)\n"
        + "Types test(char)\n"
        + "Types test(double)\n"
        + "Types test(float)\n"
        + "Types test(int)\n"
        + "Types test(long)\n"
        + "Types test(short)\n"
        + "Visibilities <init>()\n"
        + "Visibilities test1()\n"
        + "Visibilities test2()\n"
        + "Visibilities test3()\n"
        + "Visibilities test4()\n"
        + "java.lang.Object <init>()\n"
        + "java.lang.Object <init>()\n"
        + "java.lang.Object <init>()\n");
  }
}

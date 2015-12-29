package com.jakewharton.dexmethodlist;

import com.google.common.io.Resources;
import java.io.IOException;
import org.junit.Rule;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public final class MainTest {
  @Rule public final SystemOutCaptureRule out = new SystemOutCaptureRule();

  @Test public void types() throws IOException {
    System.setIn(Resources.getResource("types.dex").openStream());
    Main.main();
    assertThat(out.get()).isEqualTo(""
        + "Test <init>()\n"
        + "Test test(String)\n"
        + "Test test(String[])\n"
        + "Test test(boolean)\n"
        + "Test test(byte)\n"
        + "Test test(char)\n"
        + "Test test(double)\n"
        + "Test test(float)\n"
        + "Test test(int)\n"
        + "Test test(long)\n"
        + "Test test(short)\n"
        + "java.lang.Object <init>()\n");
  }

  @Test public void paramsJoined() throws IOException {
    System.setIn(Resources.getResource("params_joined.dex").openStream());
    Main.main();
    assertThat(out.get()).isEqualTo(""
        + "Test <init>()\n"
        + "Test test(String, String, String, String)\n"
        + "java.lang.Object <init>()\n");
  }

  @Test public void visibilities() throws IOException {
    System.setIn(Resources.getResource("visibilities.dex").openStream());
    Main.main();
    assertThat(out.get()).isEqualTo(""
        + "Test <init>()\n"
        + "Test test1()\n"
        + "Test test2()\n"
        + "Test test3()\n"
        + "Test test4()\n"
        + "java.lang.Object <init>()\n");
  }
}

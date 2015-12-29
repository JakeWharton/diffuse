package com.jakewharton.dexmethodlist;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import static java.nio.charset.StandardCharsets.UTF_8;

final class SystemOutCaptureRule implements TestRule {
  private final ByteArrayOutputStream os = new ByteArrayOutputStream();
  private final PrintStream writer = new PrintStream(os);

  public String get() {
    writer.flush();
    return new String(os.toByteArray(), UTF_8);
  }

  @Override public Statement apply(Statement base, Description description) {
    return new Statement() {
      @Override public void evaluate() throws Throwable {
        PrintStream originalOut = System.out;
        try {
          System.setOut(writer);
          base.evaluate();
        } finally {
          System.setOut(originalOut);
        }
      }
    };
  }
}

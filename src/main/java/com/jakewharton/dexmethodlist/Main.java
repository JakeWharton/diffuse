package com.jakewharton.dexmethodlist;

import com.android.dex.Dex;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public final class Main {
  public static void main(String... args) throws IOException {
    InputStream is = null;
    if (args.length == 0) {
      is = System.in;
    } else if (args.length == 1) {
      try {
        is = new FileInputStream(args[0]);
      } catch (FileNotFoundException e) {
        System.err.println("File " + args[0] + " not found.");
        System.exit(1);
      }
    } else {
      System.err.println("Expected dex filename as sole argument or as stdin.");
      System.exit(1);
    }

    Dex dex = new Dex(is);
    dex.methodIds().stream().map(methodId -> {
      StringBuilder builder = new StringBuilder();

      String typeName = dex.typeNames().get(methodId.getDeclaringClassIndex());
      String methodName = dex.strings().get(methodId.getNameIndex());
      builder.append(humanName(typeName, false)).append(' ').append(methodName);

      builder.append('(');
      short[] types =
          dex.readTypeList(dex.protoIds().get(methodId.getProtoIndex()).getParametersOffset())
              .getTypes();
      for (int i = 0; i < types.length; i++) {
        if (i > 0) builder.append(", ");
        String paramName = dex.typeNames().get(types[i]);
        builder.append(humanName(paramName, true));
      }
      builder.append(')');

      return builder.toString();
    }).sorted().forEachOrdered(System.out::println);
  }

  private static String humanName(String type, boolean stripPackage) {
    if (type.startsWith("[")) {
      return humanName(type.substring(1), stripPackage) + "[]";
    }
    if (type.startsWith("L")) {
      type = type.substring(1, type.length() - 1);
      if (stripPackage) {
        int index = type.lastIndexOf('/');
        if (index != -1) {
          type = type.substring(index + 1);
        }
      } else {
        type = type.replace('/', '.');
      }
      return type;
    }
    switch (type) {
      case "B": return "byte";
      case "C": return "char";
      case "D": return "double";
      case "F": return "float";
      case "I": return "int";
      case "J": return "long";
      case "S": return "short";
      case "V": return "void";
      case "Z": return "boolean";
      default: throw new IllegalArgumentException("Unknown type: " + type);
    }
  }

  private Main() {
    throw new AssertionError("No instances.");
  }
}

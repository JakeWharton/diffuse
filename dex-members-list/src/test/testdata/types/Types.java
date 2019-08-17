class Types {
  boolean valueBoolean;
  byte valueByte;
  char valueChar;
  double valueDouble;
  float valueFloat;
  int valueInt;
  long valueLong;
  short valueShort;
  String valueString;
  String[] valueStringArray;

  void test(boolean value) {}
  void test(byte value) {}
  void test(char value) {}
  void test(double value) {}
  void test(float value) {}
  void test(int value) {}
  void test(long value) {}
  void test(short value) {}
  void test(String value) {
    System.out.println(value);
  }
  void test(String[] value) {}

  Runnable returnsRunnable() { return null; }
  String returnsString() { return null; }
  boolean returnsBoolean() { return false; }
}

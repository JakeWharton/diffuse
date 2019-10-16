package com.jakewharton.diffuse

import com.jakewharton.diffuse.ApiMapping.Companion.toApiMapping
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Test

class ApiMappingTest {
  @Test fun commentsAndWhitespaceIgnored() {
    val mapping = """
        # Leading comment

        # Comment after empty line
    """.trimIndent().toApiMapping()

    assertNotNull(mapping)
  }

  @Test fun typeMappingWithoutMembers() {
    val mapping = """
      com.example.Foo -> a.a.a:
      com.example.Bar -> a.a.b:
    """.trimIndent().toApiMapping()

    val aaaDescriptor = TypeDescriptor("La/a/a;")
    val fooDescriptor = TypeDescriptor("Lcom/example/Foo;")
    assertEquals(fooDescriptor, mapping[aaaDescriptor])

    // Ensure trailing type mapping is included.
    val aabDescriptor = TypeDescriptor("La/a/b;")
    val barDescriptor = TypeDescriptor("Lcom/example/Bar;")
    assertEquals(barDescriptor, mapping[aabDescriptor])
  }

  @Test fun fieldNameMapping() {
    val mapping = """
      com.example.Foo -> a.a.a:
          java.lang.String bar -> a
    """.trimIndent().toApiMapping()

    val aaaDescriptor = TypeDescriptor("La/a/a;")
    val aField = DexField(aaaDescriptor, "a", stringDescriptor)

    val fooDescriptor = TypeDescriptor("Lcom/example/Foo;")
    val barField = DexField(fooDescriptor, "bar", stringDescriptor)

    assertEquals(barField, mapping[aField])
  }

  @Test fun fieldTypeMapping() {
    val mapping = """
      com.example.Foo -> a.a.a:
          com.example.Bar bar -> a
      com.example.Bar -> a.a.b:
    """.trimIndent().toApiMapping()

    val aaaDescriptor = TypeDescriptor("La/a/a;")
    val aabDescriptor = TypeDescriptor("La/a/b;")
    val aField = DexField(aaaDescriptor, "a", aabDescriptor)

    val fooDescriptor = TypeDescriptor("Lcom/example/Foo;")
    val barDescriptor = TypeDescriptor("Lcom/example/Bar;")
    val barField = DexField(fooDescriptor, "bar", barDescriptor)

    assertEquals(barField, mapping[aField])
  }

  @Test fun fieldUnmappedName() {
    val mapping = """
      com.example.Foo -> a.a.a:
    """.trimIndent().toApiMapping()

    val aaaDescriptor = TypeDescriptor("La/a/a;")
    val unmappedField = DexField(aaaDescriptor, "bar", stringDescriptor)

    val fooDescriptor = TypeDescriptor("Lcom/example/Foo;")
    val barField = DexField(fooDescriptor, "bar", stringDescriptor)

    assertEquals(barField, mapping[unmappedField])
  }

  @Test fun fieldUnmappedDeclaringType() {
    val mapping = "".toApiMapping()

    val fooDescriptor = TypeDescriptor("Lcom/example/Foo;")
    val field = DexField(fooDescriptor, "bar", stringDescriptor)
    assertSame(field, mapping[field])
  }

  @Test fun fieldUnmappedArrayDeclaringType() {
    val mapping = "".toApiMapping()

    val byteArrayLength = DexField(byteDescriptor.asArray(1), "length", intDescriptor)
    assertSame(byteArrayLength, mapping[byteArrayLength])

    val boxedByteSize = DexField(TypeDescriptor("Ljava/lang/Byte;"), "SIZE", intDescriptor)
    assertSame(boxedByteSize, mapping[boxedByteSize])
  }

  @Test fun fieldPrimitiveTypes() {
    val mapping = """
      com.example.Foo -> a.a.a:
          boolean aBoolean -> a
          byte aByte -> b
          char aChar -> c
          double aDouble -> d
          float aFloat -> e
          int aInt -> f
          long aLong -> g
          short aShort -> h
    """.trimIndent().toApiMapping()

    val aaaDescriptor = TypeDescriptor("La/a/a;")
    val aField = DexField(aaaDescriptor, "a", booleanDescriptor)
    val bField = DexField(aaaDescriptor, "b", byteDescriptor)
    val cField = DexField(aaaDescriptor, "c", charDescriptor)
    val dField = DexField(aaaDescriptor, "d", doubleDescriptor)
    val eField = DexField(aaaDescriptor, "e", floatDescriptor)
    val fField = DexField(aaaDescriptor, "f", intDescriptor)
    val gField = DexField(aaaDescriptor, "g", longDescriptor)
    val hField = DexField(aaaDescriptor, "h", shortDescriptor)

    val fooDescriptor = TypeDescriptor("Lcom/example/Foo;")
    val booleanField = DexField(fooDescriptor, "aBoolean", booleanDescriptor)
    val byteField = DexField(fooDescriptor, "aByte", byteDescriptor)
    val charField = DexField(fooDescriptor, "aChar", charDescriptor)
    val doubleField = DexField(fooDescriptor, "aDouble", doubleDescriptor)
    val floatField = DexField(fooDescriptor, "aFloat", floatDescriptor)
    val intField = DexField(fooDescriptor, "aInt", intDescriptor)
    val longField = DexField(fooDescriptor, "aLong", longDescriptor)
    val shortField = DexField(fooDescriptor, "aShort", shortDescriptor)

    assertEquals(booleanField, mapping[aField])
    assertEquals(byteField, mapping[bField])
    assertEquals(charField, mapping[cField])
    assertEquals(doubleField, mapping[dField])
    assertEquals(floatField, mapping[eField])
    assertEquals(intField, mapping[fField])
    assertEquals(longField, mapping[gField])
    assertEquals(shortField, mapping[hField])
  }

  @Test fun fieldArray() {
    val mapping = """
      com.example.Foo -> a.a.a:
          byte[] bytes -> a
          com.example.Bar[] bars -> b
          java.lang.String[][][][][][] strings -> c
      com.example.Bar -> a.a.b:
    """.trimIndent().toApiMapping()

    val aaaDescriptor = TypeDescriptor("La/a/a;")
    val aabDescriptor = TypeDescriptor("La/a/b;")
    val aField = DexField(aaaDescriptor, "a", byteDescriptor.asArray(1))
    val bField = DexField(aaaDescriptor, "b", aabDescriptor.asArray(1))
    val cField = DexField(aaaDescriptor, "c", stringDescriptor.asArray(6))

    val fooDescriptor = TypeDescriptor("Lcom/example/Foo;")
    val barDescriptor = TypeDescriptor("Lcom/example/Bar;")
    val bytesField = DexField(fooDescriptor, "bytes", byteDescriptor.asArray(1))
    val barsField = DexField(fooDescriptor, "bars", barDescriptor.asArray(1))
    val stringsField = DexField(fooDescriptor, "strings", stringDescriptor.asArray(6))

    assertEquals(bytesField, mapping[aField])
    assertEquals(barsField, mapping[bField])
    assertEquals(stringsField, mapping[cField])
  }

  @Test fun methodNameMapping() {
    val mapping = """
      com.example.Foo -> a.a.a:
          void bar() -> a
    """.trimIndent().toApiMapping()

    val aaaDescriptor = TypeDescriptor("La/a/a;")
    val aMethod = DexMethod(aaaDescriptor, "a", emptyList(), voidDescriptor)

    val fooDescriptor = TypeDescriptor("Lcom/example/Foo;")
    val barField = DexMethod(fooDescriptor, "bar", emptyList(), voidDescriptor)

    assertEquals(barField, mapping[aMethod])
  }

  @Test fun methodReturnTypeMapping() {
    val mapping = """
      com.example.Foo -> a.a.a:
          com.example.Bar bar() -> a
      com.example.Bar -> a.a.b:
    """.trimIndent().toApiMapping()

    val aaaDescriptor = TypeDescriptor("La/a/a;")
    val aabDescriptor = TypeDescriptor("La/a/b;")
    val aMethod = DexMethod(aaaDescriptor, "a", emptyList(), aabDescriptor)

    val fooDescriptor = TypeDescriptor("Lcom/example/Foo;")
    val barDescriptor = TypeDescriptor("Lcom/example/Bar;")
    val barField = DexMethod(fooDescriptor, "bar", emptyList(), barDescriptor)

    assertEquals(barField, mapping[aMethod])
  }

  @Test fun methodParameterTypeMapping() {
    val mapping = """
      com.example.Foo -> a.a.a:
          void bar(com.example.Bar) -> a
      com.example.Bar -> a.a.b:
    """.trimIndent().toApiMapping()

    val aaaDescriptor = TypeDescriptor("La/a/a;")
    val aabDescriptor = TypeDescriptor("La/a/b;")
    val aMethod = DexMethod(aaaDescriptor, "a", listOf(aabDescriptor), voidDescriptor)

    val fooDescriptor = TypeDescriptor("Lcom/example/Foo;")
    val barDescriptor = TypeDescriptor("Lcom/example/Bar;")
    val barField = DexMethod(fooDescriptor, "bar", listOf(barDescriptor), voidDescriptor)

    assertEquals(barField, mapping[aMethod])
  }

  @Test fun methodWithSourceLineNumbers() {
    val mapping = """
      com.example.Foo -> a.a.a:
          1:1:void bar() -> a
    """.trimIndent().toApiMapping()

    val aaaDescriptor = TypeDescriptor("La/a/a;")
    val aMethod = DexMethod(aaaDescriptor, "a", emptyList(), voidDescriptor)

    val fooDescriptor = TypeDescriptor("Lcom/example/Foo;")
    val barField = DexMethod(fooDescriptor, "bar", emptyList(), voidDescriptor)

    assertEquals(barField, mapping[aMethod])
  }

  @Test fun methodWithMappedLineNumbers() {
    val mapping = """
      com.example.Foo -> a.a.a:
          1:1:void bar():12:12 -> a
    """.trimIndent().toApiMapping()

    val aaaDescriptor = TypeDescriptor("La/a/a;")
    val aMethod = DexMethod(aaaDescriptor, "a", emptyList(), voidDescriptor)

    val fooDescriptor = TypeDescriptor("Lcom/example/Foo;")
    val barField = DexMethod(fooDescriptor, "bar", emptyList(), voidDescriptor)

    assertEquals(barField, mapping[aMethod])
  }

  @Test fun methodPrimitiveReturnTypes() {
    val mapping = """
      com.example.Foo -> a.a.a:
          boolean aBoolean() -> a
          byte aByte() -> b
          char aChar() -> c
          double aDouble() -> d
          float aFloat() -> e
          int aInt() -> f
          long aLong() -> g
          short aShort() -> h
    """.trimIndent().toApiMapping()

    val aaaDescriptor = TypeDescriptor("La/a/a;")
    val aMethod = DexMethod(aaaDescriptor, "a", emptyList(), booleanDescriptor)
    val bMethod = DexMethod(aaaDescriptor, "b", emptyList(), byteDescriptor)
    val cMethod = DexMethod(aaaDescriptor, "c", emptyList(), charDescriptor)
    val dMethod = DexMethod(aaaDescriptor, "d", emptyList(), doubleDescriptor)
    val eMethod = DexMethod(aaaDescriptor, "e", emptyList(), floatDescriptor)
    val fMethod = DexMethod(aaaDescriptor, "f", emptyList(), intDescriptor)
    val gMethod = DexMethod(aaaDescriptor, "g", emptyList(), longDescriptor)
    val hMethod = DexMethod(aaaDescriptor, "h", emptyList(), shortDescriptor)

    val fooDescriptor = TypeDescriptor("Lcom/example/Foo;")
    val booleanMethod = DexMethod(fooDescriptor, "aBoolean", emptyList(), booleanDescriptor)
    val byteMethod = DexMethod(fooDescriptor, "aByte", emptyList(), byteDescriptor)
    val charMethod = DexMethod(fooDescriptor, "aChar", emptyList(), charDescriptor)
    val doubleMethod = DexMethod(fooDescriptor, "aDouble", emptyList(), doubleDescriptor)
    val floatMethod = DexMethod(fooDescriptor, "aFloat", emptyList(), floatDescriptor)
    val intMethod = DexMethod(fooDescriptor, "aInt", emptyList(), intDescriptor)
    val longMethod = DexMethod(fooDescriptor, "aLong", emptyList(), longDescriptor)
    val shortMethod = DexMethod(fooDescriptor, "aShort", emptyList(), shortDescriptor)

    assertEquals(booleanMethod, mapping[aMethod])
    assertEquals(byteMethod, mapping[bMethod])
    assertEquals(charMethod, mapping[cMethod])
    assertEquals(doubleMethod, mapping[dMethod])
    assertEquals(floatMethod, mapping[eMethod])
    assertEquals(intMethod, mapping[fMethod])
    assertEquals(longMethod, mapping[gMethod])
    assertEquals(shortMethod, mapping[hMethod])
  }

  @Test fun methodPrimitiveParameterTypes() {
    val mapping = """
      com.example.Foo -> a.a.a:
          void bar(boolean,byte,char,double,float,int,long,short) -> a
    """.trimIndent().toApiMapping()

    val aaaDescriptor = TypeDescriptor("La/a/a;")
    val aMethod = DexMethod(aaaDescriptor, "a",
        listOf(booleanDescriptor, byteDescriptor, charDescriptor, doubleDescriptor, floatDescriptor,
            intDescriptor, longDescriptor, shortDescriptor), voidDescriptor)

    val fooDescriptor = TypeDescriptor("Lcom/example/Foo;")
    val barMethod = DexMethod(fooDescriptor, "bar",
        listOf(booleanDescriptor, byteDescriptor, charDescriptor, doubleDescriptor, floatDescriptor,
            intDescriptor, longDescriptor, shortDescriptor), voidDescriptor)

    assertEquals(barMethod, mapping[aMethod])
  }

  @Test fun methodArrayReturnTypes() {
    val mapping = """
      com.example.Foo -> a.a.a:
          byte[] bytes() -> a
          com.example.Bar[] bars() -> b
          java.lang.String[][][][][][] strings() -> c
      com.example.Bar -> a.a.b:
    """.trimIndent().toApiMapping()

    val aaaDescriptor = TypeDescriptor("La/a/a;")
    val aabDescriptor = TypeDescriptor("La/a/b;")
    val aMethod = DexMethod(aaaDescriptor, "a", emptyList(), byteDescriptor.asArray(1))
    val bMethod = DexMethod(aaaDescriptor, "b", emptyList(), aabDescriptor.asArray(1))
    val cMethod = DexMethod(aaaDescriptor, "c", emptyList(), stringDescriptor.asArray(6))

    val fooDescriptor = TypeDescriptor("Lcom/example/Foo;")
    val barDescriptor = TypeDescriptor("Lcom/example/Bar;")
    val bytesMethod = DexMethod(fooDescriptor, "bytes", emptyList(), byteDescriptor.asArray(1))
    val barsMethod = DexMethod(fooDescriptor, "bars", emptyList(), barDescriptor.asArray(1))
    val stringsMethod = DexMethod(fooDescriptor, "strings", emptyList(), stringDescriptor.asArray(6))

    assertEquals(bytesMethod, mapping[aMethod])
    assertEquals(barsMethod, mapping[bMethod])
    assertEquals(stringsMethod, mapping[cMethod])
  }

  @Test fun methodArrayParameterTypes() {
    val mapping = """
      com.example.Foo -> a.a.a:
          void bar(byte[],com.example.Bar[],java.lang.String[][][][][][]) -> a
      com.example.Bar -> a.a.b:
    """.trimIndent().toApiMapping()

    val aaaDescriptor = TypeDescriptor("La/a/a;")
    val aabDescriptor = TypeDescriptor("La/a/b;")
    val aMethod = DexMethod(aaaDescriptor, "a",
        listOf(byteDescriptor.asArray(1), aabDescriptor.asArray(1), stringDescriptor.asArray(6)),
        voidDescriptor)

    val fooDescriptor = TypeDescriptor("Lcom/example/Foo;")
    val barDescriptor = TypeDescriptor("Lcom/example/Bar;")
    val barMethod = DexMethod(fooDescriptor, "bar",
        listOf(byteDescriptor.asArray(1), barDescriptor.asArray(1), stringDescriptor.asArray(6)),
        voidDescriptor)

    assertEquals(barMethod, mapping[aMethod])
  }

  @Test fun methodUnmappedSignatures() {
    val mapping = "".toApiMapping()

    val boxedByteToString = DexMethod(TypeDescriptor("Ljava/lang/Byte;"), "toString", emptyList(),
        stringDescriptor)
    assertSame(boxedByteToString, mapping[boxedByteToString])
  }

  @Test fun methodUnmappedDeclaringType() {
    val mapping = "".toApiMapping()

    val byteArrayClone = DexMethod(byteDescriptor.asArray(1), "clone", emptyList(),
        byteDescriptor.asArray(1))
    assertSame(byteArrayClone, mapping[byteArrayClone])

    val boxedByteToString = DexMethod(TypeDescriptor("Ljava/lang/Byte;"), "toString",
        listOf(byteDescriptor), stringDescriptor)
    assertSame(boxedByteToString, mapping[boxedByteToString])
  }

  private val booleanDescriptor = TypeDescriptor("Z")
  private val byteDescriptor = TypeDescriptor("B")
  private val charDescriptor = TypeDescriptor("C")
  private val doubleDescriptor = TypeDescriptor("D")
  private val floatDescriptor = TypeDescriptor("F")
  private val intDescriptor = TypeDescriptor("I")
  private val longDescriptor = TypeDescriptor("J")
  private val shortDescriptor = TypeDescriptor("S")
  private val stringDescriptor = TypeDescriptor("Ljava/lang/String;")
  private val voidDescriptor = TypeDescriptor("V")
}

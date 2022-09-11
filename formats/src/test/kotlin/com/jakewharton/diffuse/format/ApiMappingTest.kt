package com.jakewharton.diffuse.format

import com.jakewharton.diffuse.format.ApiMapping.Companion.toApiMapping
import com.jakewharton.diffuse.io.Input.Companion.asInput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Test

class ApiMappingTest {
  @Test fun commentsAndWhitespaceIgnored() {
    val mapping =
      """
        # Leading comment

        # Comment after empty line
      """.trimIndent().asInput("mapping.txt").toApiMapping()

    assertNotNull(mapping)
  }

  @Test fun typeMappingWithoutMembers() {
    val mapping =
      """
      com.example.Foo -> a.a.a:
      com.example.Bar -> a.a.b:
      """.trimIndent().asInput("mapping.txt").toApiMapping()

    val aaaDescriptor = TypeDescriptor("La/a/a;")
    val fooDescriptor = TypeDescriptor("Lcom/example/Foo;")
    assertEquals(fooDescriptor, mapping[aaaDescriptor])

    // Ensure trailing type mapping is included.
    val aabDescriptor = TypeDescriptor("La/a/b;")
    val barDescriptor = TypeDescriptor("Lcom/example/Bar;")
    assertEquals(barDescriptor, mapping[aabDescriptor])
  }

  @Test fun fieldNameMapping() {
    val mapping =
      """
      com.example.Foo -> a.a.a:
          java.lang.String bar -> a
      """.trimIndent().asInput("mapping.txt").toApiMapping()

    val aaaDescriptor = TypeDescriptor("La/a/a;")
    val aField = Field(aaaDescriptor, "a", stringDescriptor)

    val fooDescriptor = TypeDescriptor("Lcom/example/Foo;")
    val barField = Field(fooDescriptor, "bar", stringDescriptor)

    assertEquals(barField, mapping[aField])
  }

  @Test fun fieldTypeMapping() {
    val mapping =
      """
      com.example.Foo -> a.a.a:
          com.example.Bar bar -> a
      com.example.Bar -> a.a.b:
      """.trimIndent().asInput("mapping.txt").toApiMapping()

    val aaaDescriptor = TypeDescriptor("La/a/a;")
    val aabDescriptor = TypeDescriptor("La/a/b;")
    val aField = Field(aaaDescriptor, "a", aabDescriptor)

    val fooDescriptor = TypeDescriptor("Lcom/example/Foo;")
    val barDescriptor = TypeDescriptor("Lcom/example/Bar;")
    val barField = Field(fooDescriptor, "bar", barDescriptor)

    assertEquals(barField, mapping[aField])
  }

  @Test fun fieldUnmappedName() {
    val mapping =
      """
      com.example.Foo -> a.a.a:
      """.trimIndent().asInput("mapping.txt").toApiMapping()

    val aaaDescriptor = TypeDescriptor("La/a/a;")
    val unmappedField = Field(aaaDescriptor, "bar", stringDescriptor)

    val fooDescriptor = TypeDescriptor("Lcom/example/Foo;")
    val barField = Field(fooDescriptor, "bar", stringDescriptor)

    assertEquals(barField, mapping[unmappedField])
  }

  @Test fun fieldUnmappedDeclaringType() {
    val mapping = "".asInput("mapping.txt").toApiMapping()

    val fooDescriptor = TypeDescriptor("Lcom/example/Foo;")
    val field = Field(fooDescriptor, "bar", stringDescriptor)
    assertSame(field, mapping[field])
  }

  @Test fun fieldUnmappedArrayDeclaringType() {
    val mapping = "".asInput("mapping.txt").toApiMapping()

    val byteArrayLength = Field(byteDescriptor.asArray(1), "length", intDescriptor)
    assertSame(byteArrayLength, mapping[byteArrayLength])

    val boxedByteSize = Field(TypeDescriptor("Ljava/lang/Byte;"), "SIZE", intDescriptor)
    assertSame(boxedByteSize, mapping[boxedByteSize])
  }

  @Test fun fieldPrimitiveTypes() {
    val mapping =
      """
      com.example.Foo -> a.a.a:
          boolean aBoolean -> a
          byte aByte -> b
          char aChar -> c
          double aDouble -> d
          float aFloat -> e
          int aInt -> f
          long aLong -> g
          short aShort -> h
      """.trimIndent().asInput("mapping.txt").toApiMapping()

    val aaaDescriptor = TypeDescriptor("La/a/a;")
    val aField = Field(aaaDescriptor, "a", booleanDescriptor)
    val bField = Field(aaaDescriptor, "b", byteDescriptor)
    val cField = Field(aaaDescriptor, "c", charDescriptor)
    val dField = Field(aaaDescriptor, "d", doubleDescriptor)
    val eField = Field(aaaDescriptor, "e", floatDescriptor)
    val fField = Field(aaaDescriptor, "f", intDescriptor)
    val gField = Field(aaaDescriptor, "g", longDescriptor)
    val hField = Field(aaaDescriptor, "h", shortDescriptor)

    val fooDescriptor = TypeDescriptor("Lcom/example/Foo;")
    val booleanField = Field(fooDescriptor, "aBoolean", booleanDescriptor)
    val byteField = Field(fooDescriptor, "aByte", byteDescriptor)
    val charField = Field(fooDescriptor, "aChar", charDescriptor)
    val doubleField = Field(fooDescriptor, "aDouble", doubleDescriptor)
    val floatField = Field(fooDescriptor, "aFloat", floatDescriptor)
    val intField = Field(fooDescriptor, "aInt", intDescriptor)
    val longField = Field(fooDescriptor, "aLong", longDescriptor)
    val shortField = Field(fooDescriptor, "aShort", shortDescriptor)

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
    val mapping =
      """
      com.example.Foo -> a.a.a:
          byte[] bytes -> a
          com.example.Bar[] bars -> b
          java.lang.String[][][][][][] strings -> c
      com.example.Bar -> a.a.b:
      """.trimIndent().asInput("mapping.txt").toApiMapping()

    val aaaDescriptor = TypeDescriptor("La/a/a;")
    val aabDescriptor = TypeDescriptor("La/a/b;")
    val aField = Field(aaaDescriptor, "a", byteDescriptor.asArray(1))
    val bField = Field(aaaDescriptor, "b", aabDescriptor.asArray(1))
    val cField = Field(aaaDescriptor, "c", stringDescriptor.asArray(6))

    val fooDescriptor = TypeDescriptor("Lcom/example/Foo;")
    val barDescriptor = TypeDescriptor("Lcom/example/Bar;")
    val bytesField = Field(fooDescriptor, "bytes", byteDescriptor.asArray(1))
    val barsField = Field(fooDescriptor, "bars", barDescriptor.asArray(1))
    val stringsField = Field(fooDescriptor, "strings", stringDescriptor.asArray(6))

    assertEquals(bytesField, mapping[aField])
    assertEquals(barsField, mapping[bField])
    assertEquals(stringsField, mapping[cField])
  }

  @Test fun methodNameMapping() {
    val mapping =
      """
      com.example.Foo -> a.a.a:
          void bar() -> a
      """.trimIndent().asInput("mapping.txt").toApiMapping()

    val aaaDescriptor = TypeDescriptor("La/a/a;")
    val aMethod = Method(aaaDescriptor, "a", emptyList(), voidDescriptor)

    val fooDescriptor = TypeDescriptor("Lcom/example/Foo;")
    val barField = Method(fooDescriptor, "bar", emptyList(), voidDescriptor)

    assertEquals(barField, mapping[aMethod])
  }

  @Test fun methodReturnTypeMapping() {
    val mapping =
      """
      com.example.Foo -> a.a.a:
          com.example.Bar bar() -> a
      com.example.Bar -> a.a.b:
      """.trimIndent().asInput("mapping.txt").toApiMapping()

    val aaaDescriptor = TypeDescriptor("La/a/a;")
    val aabDescriptor = TypeDescriptor("La/a/b;")
    val aMethod = Method(aaaDescriptor, "a", emptyList(), aabDescriptor)

    val fooDescriptor = TypeDescriptor("Lcom/example/Foo;")
    val barDescriptor = TypeDescriptor("Lcom/example/Bar;")
    val barField = Method(fooDescriptor, "bar", emptyList(), barDescriptor)

    assertEquals(barField, mapping[aMethod])
  }

  @Test fun methodParameterTypeMapping() {
    val mapping =
      """
      com.example.Foo -> a.a.a:
          void bar(com.example.Bar) -> a
      com.example.Bar -> a.a.b:
      """.trimIndent().asInput("mapping.txt").toApiMapping()

    val aaaDescriptor = TypeDescriptor("La/a/a;")
    val aabDescriptor = TypeDescriptor("La/a/b;")
    val aMethod = Method(aaaDescriptor, "a", listOf(aabDescriptor), voidDescriptor)

    val fooDescriptor = TypeDescriptor("Lcom/example/Foo;")
    val barDescriptor = TypeDescriptor("Lcom/example/Bar;")
    val barField = Method(fooDescriptor, "bar", listOf(barDescriptor), voidDescriptor)

    assertEquals(barField, mapping[aMethod])
  }

  @Test fun methodWithSourceLineNumbers() {
    val mapping =
      """
      com.example.Foo -> a.a.a:
          1:1:void bar() -> a
      """.trimIndent().asInput("mapping.txt").toApiMapping()

    val aaaDescriptor = TypeDescriptor("La/a/a;")
    val aMethod = Method(aaaDescriptor, "a", emptyList(), voidDescriptor)

    val fooDescriptor = TypeDescriptor("Lcom/example/Foo;")
    val barField = Method(fooDescriptor, "bar", emptyList(), voidDescriptor)

    assertEquals(barField, mapping[aMethod])
  }

  @Test fun methodWithMappedLineNumbers() {
    val mapping =
      """
      com.example.Foo -> a.a.a:
          1:1:void bar():12:12 -> a
      """.trimIndent().asInput("mapping.txt").toApiMapping()

    val aaaDescriptor = TypeDescriptor("La/a/a;")
    val aMethod = Method(aaaDescriptor, "a", emptyList(), voidDescriptor)

    val fooDescriptor = TypeDescriptor("Lcom/example/Foo;")
    val barField = Method(fooDescriptor, "bar", emptyList(), voidDescriptor)

    assertEquals(barField, mapping[aMethod])
  }

  @Test fun methodPrimitiveReturnTypes() {
    val mapping =
      """
      com.example.Foo -> a.a.a:
          boolean aBoolean() -> a
          byte aByte() -> b
          char aChar() -> c
          double aDouble() -> d
          float aFloat() -> e
          int aInt() -> f
          long aLong() -> g
          short aShort() -> h
      """.trimIndent().asInput("mapping.txt").toApiMapping()

    val aaaDescriptor = TypeDescriptor("La/a/a;")
    val aMethod = Method(aaaDescriptor, "a", emptyList(), booleanDescriptor)
    val bMethod = Method(aaaDescriptor, "b", emptyList(), byteDescriptor)
    val cMethod = Method(aaaDescriptor, "c", emptyList(), charDescriptor)
    val dMethod = Method(aaaDescriptor, "d", emptyList(), doubleDescriptor)
    val eMethod = Method(aaaDescriptor, "e", emptyList(), floatDescriptor)
    val fMethod = Method(aaaDescriptor, "f", emptyList(), intDescriptor)
    val gMethod = Method(aaaDescriptor, "g", emptyList(), longDescriptor)
    val hMethod = Method(aaaDescriptor, "h", emptyList(), shortDescriptor)

    val fooDescriptor = TypeDescriptor("Lcom/example/Foo;")
    val booleanMethod = Method(fooDescriptor, "aBoolean", emptyList(), booleanDescriptor)
    val byteMethod = Method(fooDescriptor, "aByte", emptyList(), byteDescriptor)
    val charMethod = Method(fooDescriptor, "aChar", emptyList(), charDescriptor)
    val doubleMethod = Method(fooDescriptor, "aDouble", emptyList(), doubleDescriptor)
    val floatMethod = Method(fooDescriptor, "aFloat", emptyList(), floatDescriptor)
    val intMethod = Method(fooDescriptor, "aInt", emptyList(), intDescriptor)
    val longMethod = Method(fooDescriptor, "aLong", emptyList(), longDescriptor)
    val shortMethod = Method(fooDescriptor, "aShort", emptyList(), shortDescriptor)

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
    val mapping =
      """
      com.example.Foo -> a.a.a:
          void bar(boolean,byte,char,double,float,int,long,short) -> a
      """.trimIndent().asInput("mapping.txt").toApiMapping()

    val aaaDescriptor = TypeDescriptor("La/a/a;")
    val aMethod = Method(
      aaaDescriptor,
      "a",
      listOf(
        booleanDescriptor,
        byteDescriptor,
        charDescriptor,
        doubleDescriptor,
        floatDescriptor,
        intDescriptor,
        longDescriptor,
        shortDescriptor,
      ),
      voidDescriptor,
    )

    val fooDescriptor = TypeDescriptor("Lcom/example/Foo;")
    val barMethod = Method(
      fooDescriptor,
      "bar",
      listOf(
        booleanDescriptor,
        byteDescriptor,
        charDescriptor,
        doubleDescriptor,
        floatDescriptor,
        intDescriptor,
        longDescriptor,
        shortDescriptor,
      ),
      voidDescriptor,
    )

    assertEquals(barMethod, mapping[aMethod])
  }

  @Test fun methodArrayReturnTypes() {
    val mapping =
      """
      com.example.Foo -> a.a.a:
          byte[] bytes() -> a
          com.example.Bar[] bars() -> b
          java.lang.String[][][][][][] strings() -> c
      com.example.Bar -> a.a.b:
      """.trimIndent().asInput("mapping.txt").toApiMapping()

    val aaaDescriptor = TypeDescriptor("La/a/a;")
    val aabDescriptor = TypeDescriptor("La/a/b;")
    val aMethod = Method(aaaDescriptor, "a", emptyList(), byteDescriptor.asArray(1))
    val bMethod = Method(aaaDescriptor, "b", emptyList(), aabDescriptor.asArray(1))
    val cMethod = Method(aaaDescriptor, "c", emptyList(), stringDescriptor.asArray(6))

    val fooDescriptor = TypeDescriptor("Lcom/example/Foo;")
    val barDescriptor = TypeDescriptor("Lcom/example/Bar;")
    val bytesMethod = Method(fooDescriptor, "bytes", emptyList(), byteDescriptor.asArray(1))
    val barsMethod = Method(fooDescriptor, "bars", emptyList(), barDescriptor.asArray(1))
    val stringsMethod = Method(fooDescriptor, "strings", emptyList(), stringDescriptor.asArray(6))

    assertEquals(bytesMethod, mapping[aMethod])
    assertEquals(barsMethod, mapping[bMethod])
    assertEquals(stringsMethod, mapping[cMethod])
  }

  @Test fun methodArrayParameterTypes() {
    val mapping =
      """
      com.example.Foo -> a.a.a:
          void bar(byte[],com.example.Bar[],java.lang.String[][][][][][]) -> a
      com.example.Bar -> a.a.b:
      """.trimIndent().asInput("mapping.txt").toApiMapping()

    val aaaDescriptor = TypeDescriptor("La/a/a;")
    val aabDescriptor = TypeDescriptor("La/a/b;")
    val aMethod = Method(
      aaaDescriptor,
      "a",
      listOf(byteDescriptor.asArray(1), aabDescriptor.asArray(1), stringDescriptor.asArray(6)),
      voidDescriptor,
    )

    val fooDescriptor = TypeDescriptor("Lcom/example/Foo;")
    val barDescriptor = TypeDescriptor("Lcom/example/Bar;")
    val barMethod = Method(
      fooDescriptor,
      "bar",
      listOf(byteDescriptor.asArray(1), barDescriptor.asArray(1), stringDescriptor.asArray(6)),
      voidDescriptor,
    )

    assertEquals(barMethod, mapping[aMethod])
  }

  @Test fun methodUnmappedSignatures() {
    val mapping = "".asInput("mapping.txt").toApiMapping()

    val boxedByteToString = Method(
      TypeDescriptor("Ljava/lang/Byte;"),
      "toString",
      emptyList(),
      stringDescriptor,
    )
    assertSame(boxedByteToString, mapping[boxedByteToString])
  }

  @Test fun methodUnmappedDeclaringType() {
    val mapping = "".asInput("mapping.txt").toApiMapping()

    val byteArrayClone = Method(
      byteDescriptor.asArray(1),
      "clone",
      emptyList(),
      byteDescriptor.asArray(1),
    )
    assertSame(byteArrayClone, mapping[byteArrayClone])

    val boxedByteToString = Method(
      TypeDescriptor("Ljava/lang/Byte;"),
      "toString",
      listOf(byteDescriptor),
      stringDescriptor,
    )
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

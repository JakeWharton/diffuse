package com.jakewharton.dex

import com.android.dex.Dex
import com.android.dex.DexFormat
import com.android.dex.FieldId
import com.android.dex.MethodId
import com.android.dx.cf.direct.DirectClassFile
import com.android.dx.cf.direct.StdAttributeFactory
import com.android.dx.dex.DexOptions
import com.android.dx.dex.cf.CfOptions
import com.android.dx.dex.cf.CfTranslator
import com.android.dx.dex.file.DexFile
import com.android.tools.r8.D8
import com.android.tools.r8.D8Command
import com.android.tools.r8.DexIndexedConsumer
import com.android.tools.r8.DiagnosticsHandler
import com.android.tools.r8.origin.Origin
import java.io.ByteArrayInputStream
import java.util.ArrayList
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

private val CLASS_MAGIC = byteArrayOf(0xCA.toByte(), 0xFE.toByte(), 0xBA.toByte(), 0xBE.toByte())
private val DEX_MAGIC = byteArrayOf(0x64, 0x65, 0x78, 0x0a, 0x30, 0x33, 0x35, 0x00)

internal fun dexes(inputs: Iterable<ByteArray>, legacyDx: Boolean = false): List<Dex> {
  val collection = inputs
      .fold(ClassAndDexCollection()) { collection, bytes ->
        if (bytes.startsWith(DEX_MAGIC)) {
          collection.dexes += bytes
        } else if (bytes.startsWith(CLASS_MAGIC)) {
          collection.classes += bytes
        } else {
          ZipInputStream(ByteArrayInputStream(bytes)).use { zis ->
            zis.entries().forEach {
              if (it.name.endsWith(".dex")) {
                collection.dexes += zis.readBytes()
              } else if (it.name.endsWith(".class") && !it.name.startsWith("META-INF/")) {
                collection.classes += zis.readBytes()
              } else if (it.name.endsWith(".jar")) {
                ZipInputStream(ByteArrayInputStream(zis.readBytes())).use { jar ->
                  jar.entries().forEach {
                    if (it.name.endsWith(".class") && !it.name.startsWith("META-INF/")) {
                      collection.classes += jar.readBytes()
                    }
                  }
                }
              }
            }
          }
        }

        collection // Pass along the mutable reference.
      }

  if (collection.classes.isNotEmpty()) {
    if (legacyDx) {
      collection.dexes += compileWithDx(collection.classes)
    } else {
      collection.dexes += compileWithD8(collection.classes)
    }
  }
  return collection.dexes.map(::Dex)
}

private fun compileWithD8(bytes: List<ByteArray>): ByteArray {
  val builder = D8Command.builder()
  bytes.forEach { builder.addClassProgramData(it, Origin.unknown()) }

  var out: ByteArray? = null
  builder.programConsumer = object : DexIndexedConsumer {
    override fun finished(diagnostics: DiagnosticsHandler) = Unit
    override fun accept(
        index: Int,
        bytes: ByteArray,
        descriptors: Set<String>,
        diagnostics: DiagnosticsHandler?
    ) {
      assert(out == null) { "More than one dex file produced" }
      out = bytes
    }
  }

  D8.run(builder.build())

  return checkNotNull(out) { "No dex file produced" }
}

private fun compileWithDx(bytes: List<ByteArray>): ByteArray {
  val dexOptions = DexOptions()
  dexOptions.targetApiLevel = DexFormat.API_NO_EXTENDED_OPCODES
  val dexFile = DexFile(dexOptions)

  bytes.forEach {
    val cf = DirectClassFile(it, "None.class", false)
    cf.setAttributeFactory(StdAttributeFactory.THE_ONE)
    CfTranslator.translate(cf, it, CfOptions(), dexOptions, dexFile)
  }

  return dexFile.toDex(null, false)
}

internal fun Dex.getMethod(methodId: MethodId): DexMethod {
  val declaringType = humanName(typeNames()[methodId.declaringClassIndex])
  val name = strings()[methodId.nameIndex]
  val methodProtoIds = protoIds()[methodId.protoIndex]
  val parameterTypes = readTypeList(methodProtoIds.parametersOffset).types
      .map { typeNames()[it.toInt()] }
      .map { humanName(it) }
  val returnType = humanName(typeNames()[methodProtoIds.returnTypeIndex])
  return DexMethod(declaringType, name, parameterTypes, returnType)
}

internal fun Dex.getField(fieldId: FieldId): DexField {
  val declaringType = humanName(typeNames()[fieldId.declaringClassIndex])
  val name = strings()[fieldId.nameIndex]
  val type = humanName(typeNames()[fieldId.typeIndex])
  return DexField(declaringType, name, type)
}

internal fun humanName(type: String): String {
  if (type.startsWith("[")) {
    return humanName(type.substring(1)) + "[]"
  }
  if (type.startsWith("L")) {
    return type.substring(1, type.length - 1).replace('/', '.')
  }
  return when (type) {
    "B" -> "byte"
    "C" -> "char"
    "D" -> "double"
    "F" -> "float"
    "I" -> "int"
    "J" -> "long"
    "S" -> "short"
    "V" -> "void"
    "Z" -> "boolean"
    else -> throw IllegalArgumentException("Unknown type $type")
  }
}

private fun ByteArray.startsWith(value: ByteArray): Boolean {
  if (value.size > size) return false
  value.forEachIndexed { i, byte ->
    if (get(i) != byte) {
      return false
    }
  }
  return true
}

private fun ZipInputStream.entries(): Sequence<ZipEntry> {
  return object : Sequence<ZipEntry> {
    override fun iterator(): Iterator<ZipEntry> {
      return object : Iterator<ZipEntry> {
        var next: ZipEntry? = null

        override fun hasNext(): Boolean {
          next = nextEntry
          return next != null
        }

        override fun next() = next!!
      }
    }
  }
}

private class ClassAndDexCollection {
  val classes = ArrayList<ByteArray>()
  val dexes = ArrayList<ByteArray>()
}

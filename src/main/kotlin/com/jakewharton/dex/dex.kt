package com.jakewharton.dex

import com.android.dex.Dex
import com.android.dex.FieldId
import com.android.dex.MethodId
import com.android.tools.r8.D8
import com.android.tools.r8.D8Command
import com.android.tools.r8.DexIndexedConsumer
import com.android.tools.r8.DiagnosticsHandler
import com.android.tools.r8.origin.Origin
import java.io.ByteArrayInputStream
import java.util.zip.ZipInputStream

internal fun dexes(inputs: Iterable<ByteArray>): List<Dex> {
  val classes = mutableListOf<ByteArray>()
  val dexes = mutableListOf<ByteArray>()

  for (input in inputs) {
    if (input.startsWith(DEX_MAGIC)) {
      dexes += input
    } else if (input.startsWith(CLASS_MAGIC)) {
      classes += input
    } else {
      ZipInputStream(ByteArrayInputStream(input)).use { zis ->
        zis.entries().forEach {
          if (it.name.endsWith(".dex")) {
            dexes += zis.readBytes()
          } else if (it.name.endsWith(".class") && !it.name.startsWith("META-INF/")) {
            classes += zis.readBytes()
          } else if (it.name.endsWith(".jar")) {
            ZipInputStream(ByteArrayInputStream(zis.readBytes())).use { jar ->
              jar.entries().forEach {
                if (it.name.endsWith(".class") && !it.name.startsWith("META-INF/")) {
                  classes += jar.readBytes()
                }
              }
            }
          }
        }
      }
    }
  }

  if (classes.isNotEmpty()) {
    dexes += compileWithD8(classes)
  }
  return dexes.map(::Dex)
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

internal fun Dex.listMembers(): List<DexMember> = listMethods() + listFields()

internal fun Dex.listMethods(): List<DexMethod> {
  return methodIds().map(::getMethod)
}

internal fun Dex.listFields(): List<DexField> {
  return fieldIds().map(::getField)
}

internal fun Dex.getMethod(methodId: MethodId): DexMethod {
  val declaringType = Descriptor(typeNames()[methodId.declaringClassIndex])
  val name = strings()[methodId.nameIndex]
  val methodProtoIds = protoIds()[methodId.protoIndex]
  val parameterTypes = readTypeList(methodProtoIds.parametersOffset).types
      .map { Descriptor(typeNames()[it.toInt()]) }
  val returnType = Descriptor(typeNames()[methodProtoIds.returnTypeIndex])
  return DexMethod(declaringType, name, parameterTypes, returnType)
}

internal fun Dex.getField(fieldId: FieldId): DexField {
  val declaringType = Descriptor(typeNames()[fieldId.declaringClassIndex])
  val name = strings()[fieldId.nameIndex]
  val type = Descriptor(typeNames()[fieldId.typeIndex])
  return DexField(declaringType, name, type)
}

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
import java.nio.file.Path
import java.util.zip.ZipInputStream

internal fun Iterable<ByteArray>.toDexes(libraryJar: Path?): List<Dex> {
  val classes = mutableListOf<ByteArray>()
  val dexes = mutableListOf<ByteArray>()

  for (input in this) {
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
    dexes += compileClassesWithD8(classes, libraryJar)
  }
  return dexes.map(::Dex)
}

private fun compileClassesWithD8(
  bytes: List<ByteArray>,
  libraryJar: Path?
): List<ByteArray> {
  val builder = D8Command.builder()

  if (libraryJar != null) {
    builder.addLibraryFiles(libraryJar)
  } else {
    builder.minApiLevel = 29
    builder.disableDesugaring = true
  }

  bytes.forEach { builder.addClassProgramData(it, Origin.unknown()) }

  val bytesList = mutableListOf<ByteArray>()
  builder.programConsumer = object : DexIndexedConsumer {
    override fun finished(diagnostics: DiagnosticsHandler) = Unit
    override fun accept(
      index: Int,
      bytes: ByteArray,
      descriptors: Set<String>,
      diagnostics: DiagnosticsHandler?
    ) {
      bytesList += bytes
    }
  }

  D8.run(builder.build())

  check(bytesList.isNotEmpty()) { "No dex file produced" }
  return bytesList
}

internal class MemberList(
  val declared: List<DexMember>,
  val referenced: List<DexMember>
) {
  val all get() = declared + referenced

  operator fun plus(other: MemberList): MemberList {
    return MemberList(declared + other.declared, referenced + other.referenced)
  }
}

internal fun ApiMapping.get(memberList: MemberList): MemberList {
  return MemberList(
      memberList.declared.map(::get),
      memberList.referenced.map(::get)
  )
}

internal fun Dex.toMemberList(): MemberList {
  val declaredTypeIndices = classDefs().map { it.typeIndex }.toSet()
  val (declaredMethods, referencedMethods) = methodIds()
      .partition { it.declaringClassIndex in declaredTypeIndices }
      .mapEach { it.map(::getMethod) }
  val (declaredFields, referencedFields) = fieldIds()
      .partition { it.declaringClassIndex in declaredTypeIndices }
      .mapEach { it.map(::getField) }
  return MemberList(declaredMethods + declaredFields, referencedMethods + referencedFields)
}

private fun Dex.getMethod(methodId: MethodId): DexMethod {
  val declaringType = TypeDescriptor(typeNames()[methodId.declaringClassIndex])
  val name = strings()[methodId.nameIndex]
  val methodProtoIds = protoIds()[methodId.protoIndex]
  val parameterTypes = readTypeList(methodProtoIds.parametersOffset).types
      .map { TypeDescriptor(typeNames()[it.toInt()]) }
  val returnType = TypeDescriptor(typeNames()[methodProtoIds.returnTypeIndex])
  return DexMethod(declaringType, name, parameterTypes, returnType)
}

private fun Dex.getField(fieldId: FieldId): DexField {
  val declaringType = TypeDescriptor(typeNames()[fieldId.declaringClassIndex])
  val name = strings()[fieldId.nameIndex]
  val type = TypeDescriptor(typeNames()[fieldId.typeIndex])
  return DexField(declaringType, name, type)
}

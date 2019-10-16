package com.jakewharton.diffuse

import com.android.dex.ClassDef
import com.android.dex.FieldId
import com.android.dex.MethodId
import com.jakewharton.diffuse.io.Input
import okio.BufferedSource
import com.android.dex.Dex as AndroidDex

class Dex private constructor(
  val strings: List<String>,
  val types: List<String>,
  val classes: List<TypeDescriptor>,
  val declaredMembers: List<DexMember>,
  val referencedMembers: List<DexMember>
) {
  val members = declaredMembers + referencedMembers

  fun withMapping(mapping: ApiMapping): Dex {
    if (mapping.isEmpty()) return this

    // TODO map types
    val mappedClasses = classes.map(mapping::get)
    val mappedDeclaredMembers = declaredMembers.map(mapping::get)
    val mappedReferencedMembers = referencedMembers.map(mapping::get)
    return Dex(strings, types, mappedClasses, mappedDeclaredMembers, mappedReferencedMembers)
  }

  companion object {
    @JvmStatic
    @JvmName("parse")
    fun Input.toDex(): Dex {
      val bytes = source().use(BufferedSource::readByteArray)
      val dex = AndroidDex(bytes)
      return dex.toDex()
    }

    internal fun AndroidDex.toDex(): Dex {
      val classes = classDefs()
          .map { TypeDescriptor(typeNames()[it.typeIndex]) }
      val declaredTypeIndices = classDefs()
          .map(ClassDef::getTypeIndex)
          .toSet()
      val (declaredMethods, referencedMethods) = methodIds()
          .partition { it.declaringClassIndex in declaredTypeIndices }
          .mapEach { it.map(::getMethod) }
      val (declaredFields, referencedFields) = fieldIds()
          .partition { it.declaringClassIndex in declaredTypeIndices }
          .mapEach { it.map(::getField) }
      val declaredMembers = declaredMethods + declaredFields
      val referencedMembers = referencedMethods + referencedFields

      return Dex(strings(), typeNames(), classes, declaredMembers, referencedMembers)
    }
  }
}

private fun AndroidDex.getMethod(methodId: MethodId): DexMethod {
  val declaringType = TypeDescriptor(typeNames()[methodId.declaringClassIndex])
  val name = strings()[methodId.nameIndex]
  val methodProtoIds = protoIds()[methodId.protoIndex]
  val parameterTypes = readTypeList(methodProtoIds.parametersOffset).types
      .map { TypeDescriptor(typeNames()[it.toInt()]) }
  val returnType = TypeDescriptor(typeNames()[methodProtoIds.returnTypeIndex])
  return DexMethod(declaringType, name, parameterTypes, returnType)
}

private fun AndroidDex.getField(fieldId: FieldId): DexField {
  val declaringType = TypeDescriptor(typeNames()[fieldId.declaringClassIndex])
  val name = strings()[fieldId.nameIndex]
  val type = TypeDescriptor(typeNames()[fieldId.typeIndex])
  return DexField(declaringType, name, type)
}

package com.jakewharton.diffuse.format

import com.android.dex.ClassDef
import com.android.dex.Dex as AndroidDex
import com.android.dex.FieldId
import com.android.dex.MethodId
import com.jakewharton.diffuse.io.Input

class Dex private constructor(
  override val filename: String,
  val strings: List<String>,
  val types: List<String>,
  val classes: List<TypeDescriptor>,
  override val declaredMembers: List<Member>,
  override val referencedMembers: List<Member>,
) : BinaryFormat, CodeBinary {
  override val members = declaredMembers + referencedMembers

  fun withMapping(mapping: ApiMapping): Dex {
    if (mapping.isEmpty()) return this

    // TODO map types
    val mappedClasses = classes.map(mapping::get)
    val mappedDeclaredMembers = declaredMembers.map(mapping::get)
    val mappedReferencedMembers = referencedMembers.map(mapping::get)
    return Dex(filename, strings, types, mappedClasses, mappedDeclaredMembers, mappedReferencedMembers)
  }

  companion object {
    @JvmStatic
    @JvmName("parse")
    fun Input.toDex(): Dex {
      val dex = AndroidDex(toByteArray())
      val classes = dex.classDefs()
        .map { TypeDescriptor(dex.typeNames()[it.typeIndex]) }
      val declaredTypeIndices = dex.classDefs()
        .map(ClassDef::getTypeIndex)
        .toSet()
      val (declaredMethods, referencedMethods) = dex.methodIds()
        .partition { it.declaringClassIndex in declaredTypeIndices }
        .mapEach { it.map(dex::getMethod) }
      val (declaredFields, referencedFields) = dex.fieldIds()
        .partition { it.declaringClassIndex in declaredTypeIndices }
        .mapEach { it.map(dex::getField) }
      val declaredMembers = declaredMethods + declaredFields
      val referencedMembers = referencedMethods + referencedFields
      return Dex(name, dex.strings(), dex.typeNames(), classes, declaredMembers, referencedMembers)
    }

    private fun <T, R> Pair<T, T>.mapEach(body: (T) -> R): Pair<R, R> = body(first) to body(second)
  }
}

private fun AndroidDex.getMethod(methodId: MethodId): Method {
  val declaringType = TypeDescriptor(typeNames()[methodId.declaringClassIndex])
  val name = strings()[methodId.nameIndex]
  val methodProtoIds = protoIds()[methodId.protoIndex]
  val parameterTypes = readTypeList(methodProtoIds.parametersOffset).types
    .map { TypeDescriptor(typeNames()[it.toInt()]) }
  val returnType = TypeDescriptor(typeNames()[methodProtoIds.returnTypeIndex])
  return Method(declaringType, name, parameterTypes, returnType)
}

private fun AndroidDex.getField(fieldId: FieldId): Field {
  val declaringType = TypeDescriptor(typeNames()[fieldId.declaringClassIndex])
  val name = strings()[fieldId.nameIndex]
  val type = TypeDescriptor(typeNames()[fieldId.typeIndex])
  return Field(declaringType, name, type)
}

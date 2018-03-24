@file:JvmName("DexMethods")

package com.jakewharton.dex

import com.android.dex.Dex
import com.android.dex.MethodId
import java.io.File

fun main(vararg args: String) {
  val configuration = Configuration.load("dex-methods-list", *args)
  dexMethods(configuration.loadInputs())
      .map { it.render(configuration.hideSyntheticNumbers) }
      .forEach(::println)
}

/** List method references in the files of any `.dex`, `.class`, `.jar`, `.aar`, or `.apk`. */
@JvmName("list")
fun dexMethods(vararg files: File) = dexMethods(files.map { it.readBytes() })

/** List method references in the bytes of any `.dex`, `.class`, `.jar`, `.aar`, or `.apk`. */
@JvmName("list")
fun dexMethods(bytes: ByteArray) = dexMethods(listOf(bytes))

/** List method references in the bytes of any `.dex`, `.class`, `.jar`, `.aar`, or `.apk`. */
@JvmName("list")
fun dexMethods(bytes: Iterable<ByteArray>) = dexes(bytes)
    .flatMap { dex -> dex.methodIds().map { dex.getMethod(it) } }
    .sorted()

private fun Dex.getMethod(methodId: MethodId): DexMethod {
  val declaringType = humanName(typeNames()[methodId.declaringClassIndex])
  val name = strings()[methodId.nameIndex]
  val methodProtoIds = protoIds()[methodId.protoIndex]
  val parameterTypes = readTypeList(methodProtoIds.parametersOffset).types
      .map { typeNames()[it.toInt()] }
      .map { humanName(it) }
  val returnType = humanName(typeNames()[methodProtoIds.returnTypeIndex])
  return DexMethod(declaringType, name, parameterTypes, returnType)
}

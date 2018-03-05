@file:JvmName("DexMethods")

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
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.util.ArrayList
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

fun main(vararg args: String) {
  val hideSyntheticNumbers = args.contains("--hide-synthetic-numbers")
  args.filter { !it.startsWith("--") }
      .map(::FileInputStream)
      .defaultIfEmpty(System.`in`)
      .map { it.use { it.readBytes() } }
      .toList()
      .let { dexMethods(it) }
      .map { it.render(hideSyntheticNumbers) }
      .forEach { println(it) }
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

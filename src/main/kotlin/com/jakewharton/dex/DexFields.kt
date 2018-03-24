@file:JvmName("DexFields")

package com.jakewharton.dex

import com.android.dex.Dex
import com.android.dex.FieldId
import java.io.File

fun main(vararg args: String) {
  val configuration = Configuration.load("dex-fields-list", *args)
  dexFields(configuration.loadInputs()).forEach(::println)
}

/** List field references in the files of any `.dex`, `.class`, `.jar`, `.aar`, or `.apk`. */
@JvmName("list")
fun dexFields(vararg files: File) = dexFields(files.map { it.readBytes() })

/** List field references in the bytes of any `.dex`, `.class`, `.jar`, `.aar`, or `.apk`. */
@JvmName("list")
fun dexFields(bytes: ByteArray) = dexFields(listOf(bytes))

/** List field references in the bytes of any `.dex`, `.class`, `.jar`, `.aar`, or `.apk`. */
@JvmName("list")
fun dexFields(bytes: Iterable<ByteArray>) = dexes(bytes)
    .flatMap { dex -> dex.fieldIds().map { dex.getField(it) } }
    .sorted()

private fun Dex.getField(fieldId: FieldId): DexField {
  val declaringType = humanName(typeNames()[fieldId.declaringClassIndex])
  val name = strings()[fieldId.nameIndex]
  val type = humanName(typeNames()[fieldId.typeIndex])
  return DexField(declaringType, name, type)
}

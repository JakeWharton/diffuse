package com.jakewharton.dexmethodlist

import com.android.dex.Dex
import com.android.dex.MethodId
import java.io.FileInputStream
import kotlin.collections.flatMap
import kotlin.collections.forEach
import kotlin.collections.isNotEmpty
import kotlin.collections.joinToString
import kotlin.collections.last
import kotlin.collections.listOf
import kotlin.collections.map
import kotlin.collections.sorted
import kotlin.text.replace
import kotlin.text.split
import kotlin.text.startsWith
import kotlin.text.substring

class Main {
  companion object {
    @JvmStatic fun main(args: Array<String>) {
      args.map { FileInputStream(it) }
          .defaultIfEmpty(System.`in`)
          .map { Dex(it) }
          .flatMap { dex ->
            dex.methodIds()
                .map { renderMethod(dex, it) }
          }
          .sorted()
          .forEach { println(it) }
    }

    private fun renderMethod(dex: Dex, methodId: MethodId): String {
      val type = humanName(dex.typeNames()[methodId.declaringClassIndex])
      val method = dex.strings()[methodId.nameIndex]
      val params = dex.readTypeList(dex.protoIds()[methodId.protoIndex].parametersOffset).types
          .map { humanName(dex.typeNames()[it.toInt()], true) }
          .joinToString(", ")
      return "$type $method($params)"
    }

    private fun humanName(type: String, stripPackage: Boolean = false): String {
      if (type.startsWith("[")) {
        return humanName(type.substring(1), stripPackage) + "[]"
      }
      if (type.startsWith("L")) {
        val name = type.substring(1, type.length - 1)
        if (stripPackage) {
          return name.split('/').last()
        }
        return name.replace('/', '.')
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

    private fun <T> List<T>.defaultIfEmpty(value: T): List<T> {
      return if (isNotEmpty()) this else listOf(value)
    }
  }
}

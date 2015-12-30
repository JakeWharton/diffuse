package com.jakewharton.dexmethodlist

import com.android.dex.Dex
import com.android.dex.MethodId
import java.io.ByteArrayInputStream
import java.io.FileInputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import kotlin.collections.flatMap
import kotlin.collections.forEach
import kotlin.collections.forEachIndexed
import kotlin.collections.isNotEmpty
import kotlin.collections.joinToString
import kotlin.collections.last
import kotlin.collections.listOf
import kotlin.collections.map
import kotlin.collections.sorted
import kotlin.sequences.filter
import kotlin.sequences.map
import kotlin.sequences.toList
import kotlin.text.endsWith
import kotlin.text.replace
import kotlin.text.split
import kotlin.text.startsWith
import kotlin.text.substring

class Main {
  companion object {
    private val dexMagic = byteArrayOf(0x64, 0x65, 0x78, 0x0a, 0x30, 0x33, 0x35, 0x00)

    @JvmStatic fun main(args: Array<String>) {
      args.map { FileInputStream(it) }
          .defaultIfEmpty(System.`in`)
          .map { it.readBytes() }
          .flatMap {
            if (it.startsWith(dexMagic)) {
              listOf(it)
            } else {
              ZipInputStream(ByteArrayInputStream(it)).use { zis ->
                zis.entries()
                    .filter { it.name.endsWith(".dex") }
                    .map { zis.readBytes() }
                    .toList()
              }
            }
          }
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
  }
}

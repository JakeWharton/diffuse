package com.jakewharton.dex

import com.android.dex.Dex
import com.android.dex.DexFormat
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

/** Extract method references from dex bytecode. */
class DexMethods private constructor() {
  companion object {
    private val CLASS_MAGIC = byteArrayOf(0xCA.toByte(), 0xFE.toByte(), 0xBA.toByte(), 0xBE.toByte())
    private val DEX_MAGIC = byteArrayOf(0x64, 0x65, 0x78, 0x0a, 0x30, 0x33, 0x35, 0x00)

    @JvmStatic fun main(vararg args: String) {
      val bytesList = args.map { FileInputStream(it) }
          .defaultIfEmpty(System.`in`)
          .map { it.readBytes() }
          .toList()
      list(bytesList).forEach { println(it) }
    }

    /** List method references in `files` of any of `.dex`, `.class`, `.jar`, or `.apk`. */
    @JvmStatic fun list(vararg files: File): List<String> = list(files.map { it.readBytes() })

    /** List method references in the `bytes` of any of `.dex`, `.class`, `.jar`, or `.apk`. */
    @JvmStatic fun list(bytes: ByteArray): List<String> = list(listOf(bytes))

    /** List method references in the bytes of any of `.dex`, `.class`, `.jar`, or `.apk`. */
    @JvmStatic fun list(bytes: Iterable<ByteArray>): List<String> {
      val collection = bytes
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
                  } else if (it.name.endsWith(".class")) {
                    collection.classes += zis.readBytes()
                  } else if (it.name.endsWith(".jar")) {
                    ZipInputStream(ByteArrayInputStream(zis.readBytes())).use { jar ->
                      jar.entries().forEach {
                        if (it.name.endsWith(".class")) {
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
        collection.dexes += classesToDex(collection.classes)
      }
      return collection.dexes
          .map { Dex(it) }
          .flatMap { dex -> dex.methodIds().map { renderMethod(dex, it) } }
          .sorted()
    }

    private fun classesToDex(bytes: List<ByteArray>): ByteArray {
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

    internal class ClassAndDexCollection {
      val classes = ArrayList<ByteArray>()
      val dexes = ArrayList<ByteArray>()
    }
  }
}

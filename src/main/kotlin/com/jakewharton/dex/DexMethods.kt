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
import org.kohsuke.args4j.Argument
import org.kohsuke.args4j.CmdLineParser
import org.kohsuke.args4j.Option
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.util.ArrayList
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

/** Extract method references from dex bytecode. */
class DexMethods private constructor() {
  /** Configuration flags populated at startup. */
  private class Config {
    @Option(name="--hide-synthetic-numbers")
    var hideSyntheticNumbers: Boolean = false

    @Option(name="--proguard-map")
    var proguardMap : String? = null

    @Argument
    var files : List<String> = ArrayList()
  }

  companion object {
    private val CLASS_MAGIC = byteArrayOf(0xCA.toByte(), 0xFE.toByte(), 0xBA.toByte(), 0xBE.toByte())
    private val DEX_MAGIC = byteArrayOf(0x64, 0x65, 0x78, 0x0a, 0x30, 0x33, 0x35, 0x00)
    private val SYNTHETIC_SUFFIX = ".*?\\$\\d+".toRegex()

    @JvmStatic fun main(vararg args: String) {
      val config = Config()
      CmdLineParser(config).parseArgument(args.toList())

      val proguardMap = config.proguardMap
      val methodMapper : ((MethodName) -> MethodName) =
          if (proguardMap != null) ProguardNameMapper(proguardMap) else ({ it })

      val bytesList = config.files
          .map { FileInputStream(it) }
          .defaultIfEmpty(System.`in`)
          .map { it.use { it.readBytes() } }
          .toList()
      list(bytesList, config.hideSyntheticNumbers, methodMapper).forEach { println(it) }
    }

    /** List method references in `files` of any of `.dex`, `.class`, `.jar`, or `.apk`. */
    @JvmStatic fun list(vararg files: File): List<String> = list(files.map { it.readBytes() })

    /** List method references in the `bytes` of any of `.dex`, `.class`, `.jar`, or `.apk`. */
    @JvmStatic fun list(bytes: ByteArray): List<String> = list(listOf(bytes))

    /** List method references in the bytes of any of `.dex`, `.class`, `.jar`, or `.apk`. */
    @JvmStatic fun list(bytes: Iterable<ByteArray>) = list(bytes, hideSyntheticNumbers = false)

    /**
     * List method references in the bytes of any of `.dex`, `.class`, `.jar`, or `.apk`,
     * optionally hiding number suffixes from synthetic methods.
     */
    @JvmStatic fun list(
            bytes: Iterable<ByteArray>, hideSyntheticNumbers: Boolean,
            methodMapper: (MethodName) -> MethodName = {it}): List<String> {
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
          .flatMap { dex -> dex.methodIds().map {
              methodMapper(renderMethod(dex, it, hideSyntheticNumbers)).toString()
          } }
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

    private fun renderMethod(dex: Dex, methodId: MethodId,
        hideSyntheticNumbers: Boolean): MethodName {
      val type = TypeName.parse(dex.typeNames()[methodId.declaringClassIndex])
      var method = dex.strings()[methodId.nameIndex]
      if (hideSyntheticNumbers && method.matches(SYNTHETIC_SUFFIX)) {
        method = method.substring(0, method.lastIndexOf('$'))
      }
      val methodProtoIds = dex.protoIds()[methodId.protoIndex]
      val params = dex.readTypeList(methodProtoIds.parametersOffset).types
          .map { TypeName.parse(dex.typeNames()[it.toInt()], true) }
      val returnType = TypeName.parse(dex.typeNames()[methodProtoIds.returnTypeIndex], true)
      return MethodName(method, type, returnType, params.toTypedArray())
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

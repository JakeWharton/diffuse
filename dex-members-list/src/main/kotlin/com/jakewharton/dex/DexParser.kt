package com.jakewharton.dex

import com.android.dex.Dex
import java.io.File
import java.nio.file.Path

/** Parser for method and field references inside of a dex file. */
class DexParser private constructor(
  private val bytes: List<ByteArray>,
  val mapping: ApiMapping = ApiMapping.EMPTY
) {
  fun withApiMapping(mapping: ApiMapping) = DexParser(bytes, mapping)

  private val dexes by lazy { dexes(bytes) }
  private val members by lazy {
    dexes.flatMap(Dex::listMembers)
        .map(mapping::get)
        .toSortedSet()
        .toList()
  }

  fun list() = members

  fun listMethods() = members.filterIsInstance<DexMethod>()
  fun listFields() = members.filterIsInstance<DexField>()

  /** @return the number of dex files parsed. */
  fun dexCount(): Int = dexes.size

  companion object {
    /** Create a [DexParser] from of any `.dex`, `.class`, `.jar`, `.aar`, or `.apk`. */
    @JvmStatic
    @JvmName("fromPath")
    fun Path.toDexParser() = DexParser(listOf(readBytes()))

    /** Create a [DexParser] from of any `.dex`, `.class`, `.jar`, `.aar`, or `.apk`. */
    @JvmStatic
    @JvmName("fromPaths")
    fun Collection<Path>.toDexParser() = DexParser(map { it.readBytes() })

    /** Create a [DexParser] from of any `.dex`, `.class`, `.jar`, `.aar`, or `.apk`. */
    @JvmStatic
    @JvmName("fromFile")
    fun File.toDexParser() = DexParser(listOf(readBytes()))

    /** Create a [DexParser] from of any `.dex`, `.class`, `.jar`, `.aar`, or `.apk`. */
    @JvmStatic
    @JvmName("fromFiles")
    fun Iterable<File>.toDexParser() = DexParser(map { it.readBytes() })

    /** Create a [DexParser] from of any `.dex`, `.class`, `.jar`, `.aar`, or `.apk`. */
    @JvmStatic
    @JvmName("fromBytes")
    fun ByteArray.toDexParser() = DexParser(listOf(copyOf()))

    /** Create a [DexParser] from of any `.dex`, `.class`, `.jar`, `.aar`, or `.apk`. */
    @JvmStatic
    @JvmName("fromBytes")
    fun Iterable<ByteArray>.toDexParser() = DexParser(map { it.copyOf() })
  }
}

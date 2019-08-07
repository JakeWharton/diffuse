package com.jakewharton.dex

import com.android.dex.Dex
import java.io.File
import java.nio.file.Path
import java.util.TreeSet

/** Parser for method and field references inside of a dex file. */
class DexParser private constructor(
  private val bytes: List<ByteArray>,
  val mapping: ApiMapping? = null
) {
  fun withApiMapping(mapping: ApiMapping?) = DexParser(bytes, mapping)

  private val dexes by lazy { dexes(bytes) }
  private val members by lazy {
    val members = dexes.flatMapTo(TreeSet(), Dex::listMembers)
    if (mapping != null) members.map(mapping::get) else members.toList()
  }

  fun list() = members

  fun listMethods() = members.filterIsInstance<DexMethod>()
  fun listFields() = members.filterIsInstance<DexField>()

  /** @return the number of dex files parsed. */
  fun dexCount(): Int = dexes.size

  companion object {
    /** Create a [DexParser] from of any `.dex`, `.class`, `.jar`, `.aar`, or `.apk`. */
    @JvmStatic fun fromPath(path: Path) = DexParser(listOf(path.readBytes()))
    /** Create a [DexParser] from of any `.dex`, `.class`, `.jar`, `.aar`, or `.apk`. */
    @JvmStatic fun fromPaths(vararg paths: Path) = fromPaths(paths.toList())
    /** Create a [DexParser] from of any `.dex`, `.class`, `.jar`, `.aar`, or `.apk`. */
    @JvmStatic fun fromPaths(paths: Collection<Path>) = DexParser(paths.map { it.readBytes() })
    /** Create a [DexParser] from of any `.dex`, `.class`, `.jar`, `.aar`, or `.apk`. */
    @JvmStatic fun fromFile(file: File) = fromBytes(listOf(file.readBytes()))
    /** Create a [DexParser] from of any `.dex`, `.class`, `.jar`, `.aar`, or `.apk`. */
    @JvmStatic fun fromFiles(vararg files: File) = fromFiles(files.toList())
    /** Create a [DexParser] from of any `.dex`, `.class`, `.jar`, `.aar`, or `.apk`. */
    @JvmStatic fun fromFiles(files: Iterable<File>) = DexParser(files.map { it.readBytes() })
    /** Create a [DexParser] from of any `.dex`, `.class`, `.jar`, `.aar`, or `.apk`. */
    @JvmStatic fun fromBytes(bytes: ByteArray) = fromBytes(listOf(bytes.copyOf()))
    /** Create a [DexParser] from of any `.dex`, `.class`, `.jar`, `.aar`, or `.apk`. */
    @JvmStatic fun fromBytes(bytes: Iterable<ByteArray>) = DexParser(bytes.map { it.copyOf() })
  }
}

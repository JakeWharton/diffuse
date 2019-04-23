package com.jakewharton.dex

import java.io.File
import java.nio.file.Path
import java.util.TreeSet

/** Parser for method and field references inside of a dex file. */
class DexParser private constructor(
  private val bytes: Iterable<ByteArray>,
  private val legacyDx: Boolean = false
) {
  private val dexes by lazy { dexes(bytes, legacyDx) }

  /** When true, the `dx` compiler will be used to compile `.class`, `.jar`, and `.aar` inputs. */
  @JvmOverloads
  fun withLegacyDx(legacyDx: Boolean = true) = DexParser(bytes, legacyDx)

  fun list() = dexes.flatMapTo(TreeSet()) { dex ->
    dex.methodIds().map(dex::getMethod) + dex.fieldIds().map(dex::getField)
  }.toList()

  fun listMethods() = dexes.flatMapTo(TreeSet()) { dex -> dex.methodIds().map(dex::getMethod) }.toList()
  fun listFields() = dexes.flatMapTo(TreeSet()) { dex -> dex.fieldIds().map(dex::getField) }.toList()

  /** @return the number of dex files parsed. */
  fun dexCount(): Int = dexes.size

  companion object {
    /** Create a [DexParser] from of any `.dex`, `.class`, `.jar`, `.aar`, or `.apk`. */
    @JvmStatic fun fromPath(path: Path) = fromBytes(listOf(path.readBytes()))
    /** Create a [DexParser] from of any `.dex`, `.class`, `.jar`, `.aar`, or `.apk`. */
    @JvmStatic fun fromPaths(vararg paths: Path) = fromPaths(paths.toList())
    /** Create a [DexParser] from of any `.dex`, `.class`, `.jar`, `.aar`, or `.apk`. */
    @JvmStatic fun fromPaths(paths: Collection<Path>) = fromBytes(paths.map { it.readBytes() })
    /** Create a [DexParser] from of any `.dex`, `.class`, `.jar`, `.aar`, or `.apk`. */
    @JvmStatic fun fromFile(file: File) = fromBytes(listOf(file.readBytes()))
    /** Create a [DexParser] from of any `.dex`, `.class`, `.jar`, `.aar`, or `.apk`. */
    @JvmStatic fun fromFiles(vararg files: File) = fromFiles(files.toList())
    /** Create a [DexParser] from of any `.dex`, `.class`, `.jar`, `.aar`, or `.apk`. */
    @JvmStatic fun fromFiles(files: Iterable<File>) = fromBytes(files.map { it.readBytes() })
    /** Create a [DexParser] from of any `.dex`, `.class`, `.jar`, `.aar`, or `.apk`. */
    @JvmStatic fun fromBytes(bytes: ByteArray) = fromBytes(listOf(bytes))
    /** Create a [DexParser] from of any `.dex`, `.class`, `.jar`, `.aar`, or `.apk`. */
    @JvmStatic fun fromBytes(bytes: Iterable<ByteArray>) = DexParser(bytes)
  }
}

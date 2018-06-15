package com.jakewharton.dex

import java.io.File
import java.nio.file.Files
import java.nio.file.Path

/** Parser for method and field references inside of a dex file. */
class DexParser private constructor(
    private val bytes: Iterable<ByteArray>,
    private val legacyDx: Boolean = false
) {
  private val dexes by lazy { dexes(bytes, legacyDx) }

  /** When true, the `dx` compiler will be used to compile `.class`, `.jar`, and `.aar` inputs. */
  @JvmOverloads
  fun withLegacyDx(legacyDx: Boolean = true) = DexParser(bytes, legacyDx)

  fun list() = dexes.flatMap { dex ->
    dex.methodIds().map(dex::getMethod) + dex.fieldIds().map(dex::getField)
  }.sorted()

  fun listMethods() = dexes.flatMap { dex -> dex.methodIds().map(dex::getMethod) }.sorted()
  fun listFields() = dexes.flatMap { dex -> dex.fieldIds().map(dex::getField) }.sorted()

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

    // TODO https://youtrack.jetbrains.com/issue/KT-18242
    private fun Path.readBytes() = Files.newInputStream(this).use { it.readBytes() }
  }
}

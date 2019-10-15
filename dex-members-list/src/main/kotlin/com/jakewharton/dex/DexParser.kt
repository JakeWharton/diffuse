package com.jakewharton.dex

import com.android.dex.Dex
import com.jakewharton.diffuse.ApiMapping
import com.jakewharton.diffuse.DexField
import com.jakewharton.diffuse.DexMember
import com.jakewharton.diffuse.DexMethod
import java.io.File
import java.nio.file.Path

/**
 * Parser for method and field references inside of a `.dex`, `.class`, `.jar`, `.aar`, or `.apk`.
 */
class DexParser private constructor(
  private val bytes: List<ByteArray>,
  private val mapping: ApiMapping = ApiMapping.EMPTY,
  private val desugaring: Desugaring = Desugaring.DISABLED
) {
  /** Configuration for language feature and newer API call desugaring. */
  data class Desugaring(
    /** The minimum API level supported. This affects how much desugaring will occur. */
    val minApiLevel: Int,
    /**
     * The library jar(s) for use in desugaring.
     *
     * Typically this is an `android.jar` from the Android SDK or `rt.jar` from the JDK.
     */
    val libraryJars: List<Path>
  ) {
    companion object {
      @JvmField
      val DISABLED = Desugaring(29, emptyList())
    }
  }

  /**
   * Return a new [DexParser] which uses the supplied [mapping] to translate types and names.
   * These mappings are produced by tools like R8 and ProGuard.
   *
   * @see ApiMapping
   */
  fun withApiMapping(mapping: ApiMapping) = DexParser(bytes, mapping, desugaring)

  /**
   * Return a new [DexParser] which will desugar language features and newer API calls using the
   * supplied [Desugaring] configuration. This is only used for `.jar`, `.aar`, and/or `.class`
   * inputs.
   *
   * @see Desugaring
   */
  fun withDesugaring(desugaring: Desugaring) = DexParser(bytes, mapping, desugaring)

  private val dexes by lazy { bytes.toDexes(desugaring) }
  private val memberList by lazy {
    dexes.map(Dex::toMemberList)
        .takeIf { it.isNotEmpty() } // TODO https://youtrack.jetbrains.com/issue/KT-33761
        ?.reduce(MemberList::plus)
        ?.let(mapping::get)
        ?: MemberList.EMPTY
  }

  fun listMembers(): List<DexMember> = memberList.all.toSortedSet().toList()
  fun listMethods(): List<DexMethod> = listMembers().filterIsInstance<DexMethod>()
  fun listFields(): List<DexField> = listMembers().filterIsInstance<DexField>()

  fun declaredMembers(): List<DexMember> = memberList.declared.toSortedSet().toList()
  fun declaredMethods(): List<DexMethod> = declaredMembers().filterIsInstance<DexMethod>()
  fun declaredFields(): List<DexField> = declaredMembers().filterIsInstance<DexField>()

  fun referencedMembers(): List<DexMember> = memberList.referenced.toSortedSet().toList()
  fun referencedMethods(): List<DexMethod> = referencedMembers().filterIsInstance<DexMethod>()
  fun referencedFields(): List<DexField> = referencedMembers().filterIsInstance<DexField>()

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

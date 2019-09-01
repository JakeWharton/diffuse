package com.jakewharton.dex

import com.android.dex.Dex
import java.io.File
import java.nio.file.Path

/** Parser for method and field references inside of a dex file. */
class DexParser private constructor(
  private val bytes: List<ByteArray>,
  private val mapping: ApiMapping = ApiMapping.EMPTY,
  private val libraryJar: Path? = null
) {
  fun withApiMapping(mapping: ApiMapping) = DexParser(bytes, mapping, libraryJar)
  fun withDesugaring(libraryJar: Path?) = DexParser(bytes, mapping, libraryJar)

  private val dexes by lazy { bytes.toDexes(libraryJar) }
  private val memberList by lazy {
    dexes.map(Dex::toMemberList)
        .reduce(MemberList::plus)
        .let(mapping::get)
  }

  @Deprecated("Prefer listMembers()", ReplaceWith("this.listMembers()"))
  fun list(): List<DexMember> = listMembers()

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

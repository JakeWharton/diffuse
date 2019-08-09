package com.jakewharton.diffuse

import com.jakewharton.dex.DexMember
import com.jakewharton.dex.listMembers
import okio.ByteString
import com.android.dex.Dex as AndroidDex

class Dex private constructor(private val bytes: ByteString) {
  private val dex: AndroidDex by lazy { AndroidDex(bytes.toByteArray()) }

  val strings: List<String> by lazy { dex.strings().sorted() }
  val types: List<String> by lazy { dex.typeNames().sorted() }
  val classes: List<String> by lazy { dex.classDefs().map { dex.typeNames()[it.typeIndex] }}
  val members: List<DexMember> by lazy { dex.listMembers() }

  companion object {
    @JvmStatic
    @JvmName("create")
    fun ByteString.toDex() = Dex(this)
  }
}

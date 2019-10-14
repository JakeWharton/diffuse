package com.jakewharton.diffuse

import com.jakewharton.dex.DexMember
import com.jakewharton.dex.MemberList
import com.jakewharton.dex.toMemberList
import com.jakewharton.diffuse.io.Input
import okio.BufferedSource
import okio.ByteString
import com.android.dex.Dex as AndroidDex

class Dex private constructor(private val bytes: ByteString) {
  private val dex: AndroidDex by lazy { AndroidDex(bytes.toByteArray()) }
  private val memberList: MemberList by lazy { dex.toMemberList() }

  val strings: List<String> get() = dex.strings()
  val types: List<String> get() = dex.typeNames()
  val classes: List<String> by lazy { dex.classDefs().map { dex.typeNames()[it.typeIndex] }}
  val members: List<DexMember> get() = memberList.all
  val declaredMembers: List<DexMember> get() = memberList.declared
  val referencedMembers: List<DexMember> get() = memberList.referenced

  companion object {
    @JvmStatic
    @JvmName("parse")
    fun Input.toDex() = Dex(source().use(BufferedSource::readByteString))
  }
}

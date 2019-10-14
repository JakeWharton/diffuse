package com.jakewharton.diffuse

import com.jakewharton.dex.DexMember
import com.jakewharton.dex.toMemberList
import com.jakewharton.diffuse.io.Input
import okio.BufferedSource
import com.android.dex.Dex as AndroidDex

class Dex private constructor(
  val strings: List<String>,
  val types: List<String>,
  val classes: List<String>,
  val members: List<DexMember>,
  val declaredMembers: List<DexMember>,
  val referencedMembers: List<DexMember>
) {
  companion object {
    @JvmStatic
    @JvmName("parse")
    fun Input.toDex(): Dex {
      val bytes = source().use(BufferedSource::readByteArray)

      val dex = AndroidDex(bytes)
      val memberList = dex.toMemberList()
      val classes = dex.classDefs().map { dex.typeNames()[it.typeIndex] }

      return Dex(
          dex.strings(), dex.typeNames(), classes, memberList.all, memberList.declared,
          memberList.referenced
      )
    }
  }
}

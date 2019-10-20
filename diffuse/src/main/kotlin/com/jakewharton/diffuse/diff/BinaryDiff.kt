package com.jakewharton.diffuse.diff

import com.jakewharton.diffuse.Aab
import com.jakewharton.diffuse.Aar
import com.jakewharton.diffuse.ApiMapping
import com.jakewharton.diffuse.Apk
import com.jakewharton.diffuse.Jar
import com.jakewharton.diffuse.report.DiffReport

interface BinaryDiff {
  fun toTextReport(): DiffReport

  companion object {
    @JvmStatic
    fun ofApk(
      oldApk: Apk,
      oldMapping: ApiMapping = ApiMapping.EMPTY,
      newApk: Apk,
      newMapping: ApiMapping = ApiMapping.EMPTY
    ): BinaryDiff = ApkDiff(oldApk, oldMapping, newApk, newMapping)

    @JvmStatic
    fun ofAab(
      oldAab: Aab,
      newAab: Aab
    ): BinaryDiff = AabDiff(oldAab, newAab)

    @JvmStatic
    fun ofAar(
      oldAar: Aar,
      oldMapping: ApiMapping = ApiMapping.EMPTY,
      newAar: Aar,
      newMapping: ApiMapping = ApiMapping.EMPTY
    ): BinaryDiff = AarDiff(oldAar, oldMapping, newAar, newMapping)

    @JvmStatic
    fun ofJar(
      oldJar: Jar,
      oldMapping: ApiMapping = ApiMapping.EMPTY,
      newJar: Jar,
      newMapping: ApiMapping = ApiMapping.EMPTY
    ): BinaryDiff = JarDiff(oldJar, oldMapping, newJar, newMapping)
  }
}

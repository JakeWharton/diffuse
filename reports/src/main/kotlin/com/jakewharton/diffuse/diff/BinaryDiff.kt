package com.jakewharton.diffuse.diff

import com.jakewharton.diffuse.format.Aab
import com.jakewharton.diffuse.format.Aar
import com.jakewharton.diffuse.format.ApiMapping
import com.jakewharton.diffuse.format.Apk
import com.jakewharton.diffuse.format.Dex
import com.jakewharton.diffuse.format.Jar
import com.jakewharton.diffuse.report.Report

interface BinaryDiff : Report.Factory {
  companion object {
    @JvmStatic
    fun ofApk(
      oldApk: Apk,
      oldMapping: ApiMapping = ApiMapping.EMPTY,
      newApk: Apk,
      newMapping: ApiMapping = ApiMapping.EMPTY,
    ): BinaryDiff = ApkDiff(oldApk, oldMapping, newApk, newMapping)

    @JvmStatic
    fun ofAab(
      oldAab: Aab,
      newAab: Aab,
    ): BinaryDiff = AabDiff(oldAab, newAab)

    @JvmStatic
    fun ofAar(
      oldAar: Aar,
      oldMapping: ApiMapping = ApiMapping.EMPTY,
      newAar: Aar,
      newMapping: ApiMapping = ApiMapping.EMPTY,
    ): BinaryDiff = AarDiff(oldAar, oldMapping, newAar, newMapping)

    @JvmStatic
    fun ofJar(
      oldJar: Jar,
      oldMapping: ApiMapping = ApiMapping.EMPTY,
      newJar: Jar,
      newMapping: ApiMapping = ApiMapping.EMPTY,
    ): BinaryDiff = JarDiff(oldJar, oldMapping, newJar, newMapping)

    @JvmStatic
    fun ofDex(
      oldDex: Dex,
      oldMapping: ApiMapping = ApiMapping.EMPTY,
      newDex: Dex,
      newMapping: ApiMapping = ApiMapping.EMPTY,
    ): BinaryDiff = DexDiff(
      listOf(oldDex.withMapping(oldMapping)),
      listOf(newDex.withMapping(newMapping)),
    )
  }
}

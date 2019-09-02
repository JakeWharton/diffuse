package com.jakewharton.diffuse

import com.jakewharton.dex.ApiMapping

class ApkDiff(
  val oldApk: Apk,
  val oldMapping: ApiMapping = ApiMapping.EMPTY,
  val newApk: Apk,
  val newMapping: ApiMapping = ApiMapping.EMPTY
) : Diff {
  val archive: ArchiveDiff by lazy { ArchiveDiff(oldApk.files, newApk.files) }
  val dex: DexDiff by lazy { DexDiff(oldApk.dexes, oldMapping, newApk.dexes, newMapping) }
  val arsc: ArscDiff by lazy { ArscDiff(oldApk.arsc, newApk.arsc) }

  override fun toTextReport(): DiffReport = ApkDiffTextReport(this)
}

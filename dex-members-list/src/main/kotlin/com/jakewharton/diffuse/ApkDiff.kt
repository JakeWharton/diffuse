package com.jakewharton.diffuse

import com.jakewharton.dex.ApiMapping

internal class ApkDiff(
  val oldApk: Apk,
  val oldMapping: ApiMapping,
  val newApk: Apk,
  val newMapping: ApiMapping
) : Diff {
  val archive = ArchiveDiff(oldApk.files, newApk.files)
  val signatures = SignaturesDiff(oldApk.signatures, newApk.signatures)
  val dex = DexDiff(oldApk.dexes, oldMapping, newApk.dexes, newMapping)
  val arsc = ArscDiff(oldApk.arsc, newApk.arsc)

  override fun toTextReport(): DiffReport = ApkDiffTextReport(
      this)
}

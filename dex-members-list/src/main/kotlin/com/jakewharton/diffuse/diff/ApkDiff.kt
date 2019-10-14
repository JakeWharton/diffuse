package com.jakewharton.diffuse.diff

import com.jakewharton.dex.ApiMapping
import com.jakewharton.diffuse.Apk
import com.jakewharton.diffuse.report.text.ApkDiffTextReport

internal class ApkDiff(
  val oldApk: Apk,
  val oldMapping: ApiMapping,
  val newApk: Apk,
  val newMapping: ApiMapping
) : BinaryDiff {
  val archive = ArchiveFilesDiff(oldApk.files, newApk.files)
  val signatures = SignaturesDiff(oldApk.signatures, newApk.signatures)
  val dex = DexDiff(oldApk.dexes, oldMapping, newApk.dexes, newMapping)
  val arsc = ArscDiff(oldApk.arsc, newApk.arsc)
  val manifest = AndroidManifestDiff(oldApk.manifest, newApk.manifest)

  override fun toTextReport(): DiffReport = ApkDiffTextReport(this)
}

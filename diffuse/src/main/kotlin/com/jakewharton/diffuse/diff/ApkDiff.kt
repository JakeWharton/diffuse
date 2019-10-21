package com.jakewharton.diffuse.diff

import com.jakewharton.diffuse.ApiMapping
import com.jakewharton.diffuse.Apk
import com.jakewharton.diffuse.report.DiffReport
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
  val manifest = ManifestDiff(oldApk.manifest, newApk.manifest)

  val lintMessages = listOfNotNull(
      archive.checkResourcesArscCompression())

  override fun toTextReport(): DiffReport = ApkDiffTextReport(this)
}

private fun ArchiveFilesDiff.checkResourcesArscCompression(): String? {
  val oldCompressed = oldFiles["resources.arsc"]!!.isCompressed
  val newCompressed = newFiles["resources.arsc"]!!.isCompressed
  return when {
    !newCompressed && !oldCompressed -> null
    newCompressed -> "resources.arsc changed from correctly uncompressed to incorrectly compressed"
    oldCompressed -> "resources.arsc changed from incorrectly compressed to correctly uncompressed"
    else -> "resources.arsc remains incorrectly compressed instead of correctly uncompressed"
  }
}

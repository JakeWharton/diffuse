package com.jakewharton.diffuse.diff

import com.jakewharton.diffuse.diff.lint.resourcesArscCompression
import com.jakewharton.diffuse.format.ApiMapping
import com.jakewharton.diffuse.format.Apk
import com.jakewharton.diffuse.report.Report
import com.jakewharton.diffuse.report.text.ApkDiffTextReport

internal class ApkDiff(
  val oldApk: Apk,
  val oldMapping: ApiMapping,
  val newApk: Apk,
  val newMapping: ApiMapping,
) : BinaryDiff {
  val archive = ArchiveFilesDiff(oldApk.files, newApk.files)
  val signatures = SignaturesDiff(oldApk.signatures, newApk.signatures)
  val dex = DexDiff(
    oldApk.dexes.map { it.withMapping(oldMapping) },
    newApk.dexes.map { it.withMapping(newMapping) },
  )
  val arsc = ArscDiff(oldApk.arsc, newApk.arsc)
  val manifest = ManifestDiff(oldApk.manifest, newApk.manifest)

  val lintMessages = listOfNotNull(
    archive.resourcesArscCompression(),
  )

  override fun toTextReport(): Report = ApkDiffTextReport(this)
}

package com.jakewharton.diffuse.diff

import com.jakewharton.dex.ApiMapping
import com.jakewharton.diffuse.Aar
import com.jakewharton.diffuse.report.DiffReport
import com.jakewharton.diffuse.report.text.AarDiffTextReport

internal class AarDiff(
  val oldAar: Aar,
  val oldMapping: ApiMapping,
  val newAar: Aar,
  val newMapping: ApiMapping
) : BinaryDiff {
  val archive = ArchiveFilesDiff(oldAar.files, newAar.files)

  val manifest = AndroidManifestDiff(oldAar.manifest, newAar.manifest)

  override fun toTextReport(): DiffReport = AarDiffTextReport(this)
}

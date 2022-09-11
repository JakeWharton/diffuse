package com.jakewharton.diffuse.diff

import com.jakewharton.diffuse.format.Aar
import com.jakewharton.diffuse.format.ApiMapping
import com.jakewharton.diffuse.report.Report
import com.jakewharton.diffuse.report.text.AarDiffTextReport

internal class AarDiff(
  val oldAar: Aar,
  val oldMapping: ApiMapping,
  val newAar: Aar,
  val newMapping: ApiMapping,
) : BinaryDiff {
  val archive = ArchiveFilesDiff(oldAar.files, newAar.files, includeCompressed = false)
  val jars = JarsDiff(oldAar.jars, oldMapping, newAar.jars, newMapping)
  val manifest = ManifestDiff(oldAar.manifest, newAar.manifest)

  override fun toTextReport(): Report = AarDiffTextReport(this)
}

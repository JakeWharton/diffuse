package com.jakewharton.diffuse

import com.jakewharton.dex.ApiMapping

internal class AarDiff(
  val oldAar: Aar,
  val oldMapping: ApiMapping,
  val newAar: Aar,
  val newMapping: ApiMapping
) : Diff {
  val archive = ArchiveDiff(oldAar.files, newAar.files)

  val manifest = AndroidManifestDiff(oldAar.manifest, newAar.manifest)

  override fun toTextReport(): DiffReport = AarDiffTextReport(this)
}

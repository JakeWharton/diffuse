package com.jakewharton.diffuse

import com.jakewharton.dex.ApiMapping

internal class JarDiff(
  val oldJar: Jar,
  val oldMapping: ApiMapping,
  val newJar: Jar,
  val newMapping: ApiMapping
) : BinaryDiff {
  val archive = ArchiveFilesDiff(oldJar.files, newJar.files)

  override fun toTextReport(): DiffReport = JarDiffTextReport(this)
}

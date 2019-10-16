package com.jakewharton.diffuse.diff

import com.jakewharton.diffuse.ApiMapping
import com.jakewharton.diffuse.Jar
import com.jakewharton.diffuse.report.DiffReport
import com.jakewharton.diffuse.report.text.JarDiffTextReport

internal class JarDiff(
  val oldJar: Jar,
  val oldMapping: ApiMapping,
  val newJar: Jar,
  val newMapping: ApiMapping
) : BinaryDiff {
  val archive = ArchiveFilesDiff(oldJar.files, newJar.files)

  override fun toTextReport(): DiffReport = JarDiffTextReport(this)
}

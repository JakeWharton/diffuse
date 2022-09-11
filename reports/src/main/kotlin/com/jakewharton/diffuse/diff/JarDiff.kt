package com.jakewharton.diffuse.diff

import com.jakewharton.diffuse.format.ApiMapping
import com.jakewharton.diffuse.format.Jar
import com.jakewharton.diffuse.report.Report
import com.jakewharton.diffuse.report.text.JarDiffTextReport

internal class JarDiff(
  val oldJar: Jar,
  val oldMapping: ApiMapping,
  val newJar: Jar,
  val newMapping: ApiMapping,
) : BinaryDiff {
  val archive = ArchiveFilesDiff(oldJar.files, newJar.files, includeCompressed = false)
  val jars = JarsDiff(listOf(oldJar), oldMapping, listOf(newJar), newMapping)

  val changed = jars.changed || archive.changed

  override fun toTextReport(): Report = JarDiffTextReport(this)
}

package com.jakewharton.diffuse.diff

import com.jakewharton.diffuse.Aab
import com.jakewharton.diffuse.report.text.AabDiffTextReport

internal class AabDiff(
  val oldAab: Aab,
  val newAab: Aab
) : BinaryDiff {
  override fun toTextReport(): DiffReport = AabDiffTextReport(this)
}

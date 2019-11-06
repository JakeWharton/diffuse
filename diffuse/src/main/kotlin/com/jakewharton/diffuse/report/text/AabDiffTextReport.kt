package com.jakewharton.diffuse.report.text

import com.jakewharton.diffuse.diff.AabDiff
import com.jakewharton.diffuse.report.DiffReport

internal class AabDiffTextReport(private val aabDiff: AabDiff) : DiffReport {
  override fun write(appendable: Appendable) {
    appendable.apply {
      append("OLD: ")
      appendln(aabDiff.oldAab.filename)

      append("NEW: ")
      appendln(aabDiff.newAab.filename)

      appendln()
    }
  }

  override fun toString() = buildString { write(this) }
}

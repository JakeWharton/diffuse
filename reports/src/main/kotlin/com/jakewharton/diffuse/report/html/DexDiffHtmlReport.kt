package com.jakewharton.diffuse.report.html

import com.jakewharton.diffuse.diff.DexDiff
import com.jakewharton.diffuse.report.Report

internal class DexDiffHtmlReport(private val dexDiff: DexDiff) : Report {
  override fun write(appendable: Appendable) {
    TODO("Not yet implemented")
  }

  override fun toString() = buildString { write(this) }
}

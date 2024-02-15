package com.jakewharton.diffuse.report.text

import com.jakewharton.diffuse.diff.DexDiff
import com.jakewharton.diffuse.diff.toDetailReport
import com.jakewharton.diffuse.report.Report

internal class DexDiffTextReport(private val dexDiff: DexDiff) : Report {
  private val oldDex = requireNotNull(dexDiff.oldDexes.singleOrNull()) {
    "Dex diff report only supports a single old dex. Found: ${dexDiff.oldDexes}"
  }
  private val newDex = requireNotNull(dexDiff.newDexes.singleOrNull()) {
    "Dex diff report only supports a single new dex. Found: ${dexDiff.newDexes}"
  }

  override fun write(appendable: Appendable) {
    appendable.apply {
      append("OLD: ")
      appendLine(oldDex.filename)
      append("NEW: ")
      appendLine(newDex.filename)
      appendLine()
      appendLine(dexDiff.toDetailReport())
    }
  }

  override fun toString() = buildString { write(this) }
}

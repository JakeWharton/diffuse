package com.jakewharton.diffuse.report.text

import com.jakewharton.diffuse.format.Dex
import com.jakewharton.diffuse.info.toSummaryTable
import com.jakewharton.diffuse.report.Report

internal class DexInfoTextReport(private val dex: Dex) : Report {
  override fun write(appendable: Appendable) {
    appendable.apply {
      appendLine(dex.filename)
      appendLine()
      appendLine(listOf(dex).toSummaryTable())
    }
  }

  override fun toString() = buildString { write(this) }
}

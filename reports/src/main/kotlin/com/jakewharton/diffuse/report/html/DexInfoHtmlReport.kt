package com.jakewharton.diffuse.report.html

import com.jakewharton.diffuse.format.Dex
import com.jakewharton.diffuse.info.toSummaryTable
import com.jakewharton.diffuse.report.Report
import kotlinx.html.body
import kotlinx.html.h2
import kotlinx.html.head
import kotlinx.html.html
import kotlinx.html.stream.appendHTML

internal class DexInfoHtmlReport(private val dex: Dex) : Report {
  override fun write(appendable: Appendable) {
    appendable.appendHTML().html {
      head { applyStyles() }

      body {
        h2 { +dex.filename }

        toSummaryTable(listOf(dex))
      }
    }
  }

  override fun toString() = buildString { write(this) }
}

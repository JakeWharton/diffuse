package com.jakewharton.diffuse.report.html

import com.jakewharton.diffuse.diff.DexDiff
import com.jakewharton.diffuse.diff.toDetailReport
import com.jakewharton.diffuse.report.Report
import kotlinx.html.body
import kotlinx.html.br
import kotlinx.html.head
import kotlinx.html.html
import kotlinx.html.span
import kotlinx.html.stream.appendHTML

internal class DexDiffHtmlReport(private val dexDiff: DexDiff) : Report {
  private val oldDex = requireNotNull(dexDiff.oldDexes.singleOrNull()) {
    "Dex diff report only supports a single old dex. Found: ${dexDiff.oldDexes}"
  }
  private val newDex = requireNotNull(dexDiff.newDexes.singleOrNull()) {
    "Dex diff report only supports a single new dex. Found: ${dexDiff.newDexes}"
  }

  override fun write(appendable: Appendable) {
    appendable.appendHTML().html {
      head { applyStyles() }

      body {
        span { +"OLD: ${oldDex.filename}" }
        br()
        span { +"NEW: ${newDex.filename}" }
        br()

        toDetailReport(dexDiff)
      }
    }
  }

  override fun toString() = buildString { write(this) }
}

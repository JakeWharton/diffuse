package com.jakewharton.diffuse.info

import com.jakewharton.diffuse.diffuseTable
import com.jakewharton.diffuse.format.Arsc
import com.jakewharton.picnic.TextAlignment
import com.jakewharton.picnic.renderText

internal fun Arsc.toSummaryTable() = diffuseTable {
  header {
    row("ARSC", "count")
  }

  body {
    cellStyle {
      alignment = TextAlignment.MiddleRight
    }

    row("configs", configs.size)
    row("entries", entries.size)
  }
}.renderText()

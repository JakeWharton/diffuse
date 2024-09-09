package com.jakewharton.diffuse.info

import com.jakewharton.diffuse.diffuseTable
import com.jakewharton.diffuse.format.Arsc
import com.jakewharton.picnic.TextAlignment
import com.jakewharton.picnic.renderText
import kotlinx.html.FlowContent
import kotlinx.html.style
import kotlinx.html.table
import kotlinx.html.tbody
import kotlinx.html.td
import kotlinx.html.thead
import kotlinx.html.tr

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

internal fun FlowContent.toSummaryTable(arsc: Arsc) {
  table {
    thead {
      tr {
        td { +"ARSC" }
        td { +"count" }
      }
    }

    tbody {
      style = "text-align: right; vertical-align: center;"

      tr {
        td { +"configs" }
        td { +arsc.configs.size }
      }

      tr {
        td { +"entries" }
        td { +arsc.entries.size }
      }
    }
  }
}

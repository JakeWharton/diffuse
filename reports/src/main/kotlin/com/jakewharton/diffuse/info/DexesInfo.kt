package com.jakewharton.diffuse.info

import com.jakewharton.diffuse.diffuseTable
import com.jakewharton.diffuse.format.Dex
import com.jakewharton.diffuse.format.Field
import com.jakewharton.diffuse.format.Method
import com.jakewharton.picnic.TextAlignment
import com.jakewharton.picnic.renderText
import kotlinx.html.FlowContent
import kotlinx.html.TBODY
import kotlinx.html.style
import kotlinx.html.table
import kotlinx.html.tbody
import kotlinx.html.td
import kotlinx.html.thead
import kotlinx.html.tr

internal fun List<Dex>.toSummaryTable() = diffuseTable {
  val isMultidex = size > 1

  header {
    row {
      cell("DEX") {
        alignment = TextAlignment.BottomLeft
      }
      if (isMultidex) {
        cell("raw")
        cell("unique")
      } else {
        cell("count")
      }
    }
  }

  body {
    cellStyle {
      alignment = TextAlignment.MiddleRight
    }

    row {
      cell("files")
      cell(size)
      if (isMultidex) {
        // Add empty cell to ensure borders get drawn
        cell("")
      }
    }

    fun addDexRow(name: String, selector: (Dex) -> List<Any>) = row {
      cell(name)
      if (isMultidex) {
        cell(sumOf { selector(it).size })
      }
      cell(flatMapTo(LinkedHashSet(), selector).size)
    }

    addDexRow("strings") { it.strings }
    addDexRow("types") { it.types }
    addDexRow("classes") { it.classes }
    addDexRow("methods") { it.members.filterIsInstance<Method>() }
    addDexRow("fields") { it.members.filterIsInstance<Field>() }
  }
}.renderText()

internal fun FlowContent.toSummaryTable(dexList: List<Dex>) {
  val isMultidex = dexList.size > 1

  table {
    thead {
      tr {
        td {
          style = "text-align: left; vertical-align: bottom;"
          +"DEX"
        }

        if (isMultidex) {
          td { +"raw" }
          td { +"unique" }
        } else {
          td { +"count" }
        }
      }
    }

    tbody {
      style = "text-align: right; vertical-align: center;"

      tr {
        td { +"files" }
        td { +dexList.size.toString() }

        // todo: is this necessary for HTML tables? Kinda think no...
        if (isMultidex) {
          td { +"" }
        }
      }

      fun TBODY.addDexRow(name: String, selector: (Dex) -> List<Any>) {
        tr {
          td { +name }
          if (isMultidex) {
            td { +dexList.sumOf { selector(it).size } }
          }
          td { +dexList.flatMapTo(LinkedHashSet(), selector).size.toString() }
        }
      }

      addDexRow("strings") { it.strings }
      addDexRow("types") { it.types }
      addDexRow("classes") { it.classes }
      addDexRow("methods") { it.members.filterIsInstance<Method>() }
      addDexRow("fields") { it.members.filterIsInstance<Field>() }
    }
  }
}

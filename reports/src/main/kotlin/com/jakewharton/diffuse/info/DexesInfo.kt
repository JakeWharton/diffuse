package com.jakewharton.diffuse.info

import com.jakewharton.diffuse.diffuseTable
import com.jakewharton.diffuse.format.Dex
import com.jakewharton.diffuse.format.Field
import com.jakewharton.diffuse.format.Method
import com.jakewharton.picnic.TextAlignment
import com.jakewharton.picnic.renderText

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

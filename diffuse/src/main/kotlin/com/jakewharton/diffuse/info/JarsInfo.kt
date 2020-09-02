package com.jakewharton.diffuse.info

import com.jakewharton.diffuse.diffuseTable
import com.jakewharton.diffuse.format.Field
import com.jakewharton.diffuse.format.Jar
import com.jakewharton.diffuse.format.Method
import com.jakewharton.picnic.TextAlignment
import com.jakewharton.picnic.renderText

internal fun List<Jar>.toSummaryTable(name: String) = diffuseTable {
  header {
    row(name, "count")
  }

  body {
    cellStyle {
      alignment = TextAlignment.MiddleRight
    }

    fun addRow(name: String, selector: (Jar) -> List<Any>) {
      row(name, flatMap(selector).size)
    }

    // TODO addRow("strings", strings)?
    addRow("classes", Jar::classes)
    addRow("methods") { it.members.filterIsInstance<Method>() }
    addRow("fields") { it.members.filterIsInstance<Field>() }
  }
}.renderText()

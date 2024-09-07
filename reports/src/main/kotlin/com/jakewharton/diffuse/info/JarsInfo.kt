package com.jakewharton.diffuse.info

import com.jakewharton.diffuse.diffuseTable
import com.jakewharton.diffuse.format.Field
import com.jakewharton.diffuse.format.Jar
import com.jakewharton.diffuse.format.Method
import com.jakewharton.picnic.TextAlignment
import com.jakewharton.picnic.renderText
import kotlinx.html.FlowContent
import kotlinx.html.style
import kotlinx.html.table
import kotlinx.html.tbody
import kotlinx.html.th
import kotlinx.html.thead
import kotlinx.html.tr

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

internal fun FlowContent.toSummaryTable(name: String, jars: List<Jar>) {
  table {
    thead {
      tr {
        th { +name }
        th { +"count" }
      }
    }

    tbody {
      style = "text-align: right; vertical-align: middle;"

      fun addRow(name: String, selector: (Jar) -> List<Any>) {
        tr {
          th { +name }
          th { +jars.flatMap(selector).size.toString() }
        }
      }

      // TODO addRow("strings", strings)?
      addRow("classes", Jar::classes)
      addRow("methods") { it.members.filterIsInstance<Method>() }
      addRow("fields") { it.members.filterIsInstance<Field>() }
    }
  }
}

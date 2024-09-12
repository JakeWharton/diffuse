package com.jakewharton.diffuse.diff

import com.jakewharton.diffuse.diffuseTable
import com.jakewharton.diffuse.format.ApiMapping
import com.jakewharton.diffuse.format.Class
import com.jakewharton.diffuse.format.Field
import com.jakewharton.diffuse.format.Jar
import com.jakewharton.diffuse.format.Method
import com.jakewharton.diffuse.report.toDiffString
import com.jakewharton.picnic.TextAlignment.MiddleLeft
import com.jakewharton.picnic.TextAlignment.MiddleRight
import com.jakewharton.picnic.renderText
import kotlinx.html.FlowContent
import kotlinx.html.TBODY
import kotlinx.html.style
import kotlinx.html.table
import kotlinx.html.tbody
import kotlinx.html.td
import kotlinx.html.th
import kotlinx.html.thead
import kotlinx.html.tr

internal class JarsDiff(
  val oldJars: List<Jar>,
  val oldMapping: ApiMapping,
  val newJars: List<Jar>,
  val newMapping: ApiMapping,
) {
  val classes = componentDiff(oldJars, newJars) { it.classes.map(Class::descriptor) }
  val methods = componentDiff(oldJars, newJars) { it.members.filterIsInstance<Method>() }
  val declaredMethods = componentDiff(oldJars, newJars) { it.declaredMembers.filterIsInstance<Method>() }
  val referencedMethods = componentDiff(oldJars, newJars) { it.referencedMembers.filterIsInstance<Method>() }
  val fields = componentDiff(oldJars, newJars) { it.members.filterIsInstance<Field>() }
  val declaredFields = componentDiff(oldJars, newJars) { it.declaredMembers.filterIsInstance<Field>() }
  val referencedFields = componentDiff(oldJars, newJars) { it.referencedMembers.filterIsInstance<Field>() }

  val changed = methods.changed || fields.changed
}

internal fun JarsDiff.toSummaryTable(name: String) = diffuseTable {
  header {
    row {
      cell(name)
      cell("old")
      cell("new")
      cell("diff") {
        columnSpan = 2
      }
    }
  }

  body {
    cellStyle {
      alignment = MiddleRight
    }

    fun addRow(name: String, diff: ComponentDiff<*>) = row {
      cell(name)
      cell(diff.oldCount)
      cell(diff.newCount)
      cell((diff.added.size - diff.removed.size).toDiffString()) {
        borderRight = false
      }

      val addedSize = diff.added.size.toDiffString(zeroSign = '+')
      val removedSize = (-diff.removed.size).toDiffString(zeroSign = '-')
      cell("($addedSize $removedSize)") {
        borderLeft = false
        paddingLeft = 0
        alignment = MiddleLeft
      }
    }

    // TODO addRow("strings", strings)?
    addRow("classes", classes)
    addRow("methods", methods)
    addRow("fields", fields)
  }
}.renderText()

internal fun JarsDiff.toDetailReport() = buildString {
  // TODO appendComponentDiff("STRINGS", strings)?
  appendComponentDiff("CLASSES", classes)
  appendComponentDiff("METHODS", methods)
  appendComponentDiff("FIELDS", fields)
}

internal fun FlowContent.toHtmlSummary(name: String, diff: JarsDiff) {
  table {
    thead {
      tr {
        th { +name }
        th { +"old" }
        th { +"new" }
        th {
          colSpan = "2"
          +"diff"
        }
      }
    }

    tbody {
      fun TBODY.addRow(name: String, diff: ComponentDiff<*>) {
        tr {
          style = "text-align: right; vertical-align: middle;"
          td { +name }
          td { +diff.oldCount.toString() }
          td { +diff.newCount.toString() }
          td {
            style = "border-right: none;"
            +(diff.added.size - diff.removed.size).toDiffString()
          }

          val addedSize = diff.added.size.toDiffString(zeroSign = '+')
          val removedSize = (-diff.removed.size).toDiffString(zeroSign = '-')
          td {
            style = "border-left: none; padding-left: 0; text-align: left;"
            +"($addedSize $removedSize)"
          }
        }
      }

      // TODO addRow("strings", strings)?
      addRow("classes", diff.classes)
      addRow("methods", diff.methods)
      addRow("fields", diff.fields)
    }
  }
}

internal fun FlowContent.toDetailReport(diff: JarsDiff) {
  // TODO appendComponentDiff("STRINGS", strings)?
  appendComponentDiff("CLASSES", diff.classes)
  appendComponentDiff("METHODS", diff.methods)
  appendComponentDiff("FIELDS", diff.fields)
}

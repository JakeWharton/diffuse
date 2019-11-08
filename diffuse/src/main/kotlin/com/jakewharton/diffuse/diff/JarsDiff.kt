package com.jakewharton.diffuse.diff

import com.jakewharton.diffuse.ApiMapping
import com.jakewharton.diffuse.Class
import com.jakewharton.diffuse.Field
import com.jakewharton.diffuse.Jar
import com.jakewharton.diffuse.Method
import com.jakewharton.diffuse.diffuseTable
import com.jakewharton.diffuse.report.toDiffString
import com.jakewharton.picnic.TextAlignment.MiddleLeft
import com.jakewharton.picnic.TextAlignment.MiddleRight
import com.jakewharton.picnic.renderText

internal class JarsDiff(
  val oldJars: List<Jar>,
  val oldMapping: ApiMapping,
  val newJars: List<Jar>,
  val newMapping: ApiMapping
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

internal fun JarsDiff.toSummaryTable() = diffuseTable {
  header {
    row {
      cell("JAR")
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

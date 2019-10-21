package com.jakewharton.diffuse.diff

import com.jakewharton.diffuse.ApiMapping
import com.jakewharton.diffuse.Field
import com.jakewharton.diffuse.Jar
import com.jakewharton.diffuse.Method
import com.jakewharton.diffuse.diffuseTable
import com.jakewharton.diffuse.report.DiffReport
import com.jakewharton.diffuse.report.text.JarDiffTextReport
import com.jakewharton.diffuse.report.toDiffString
import com.jakewharton.picnic.TextAlignment.MiddleLeft
import com.jakewharton.picnic.TextAlignment.MiddleRight
import com.jakewharton.picnic.renderText

internal class JarDiff(
  val oldJar: Jar,
  val oldMapping: ApiMapping,
  val newJar: Jar,
  val newMapping: ApiMapping
) : BinaryDiff {
  val archive = ArchiveFilesDiff(oldJar.files, newJar.files)

  val methods = componentDiff(listOf(oldJar), listOf(newJar)) { it.members.filterIsInstance<Method>() }
  val declaredMethods = componentDiff(listOf(oldJar), listOf(newJar)) { it.declaredMembers.filterIsInstance<Method>() }
  val referencedMethods = componentDiff(listOf(oldJar), listOf(newJar)) { it.referencedMembers.filterIsInstance<Method>() }
  val fields = componentDiff(listOf(oldJar), listOf(newJar)) { it.members.filterIsInstance<Field>() }
  val declaredFields = componentDiff(listOf(oldJar), listOf(newJar)) { it.declaredMembers.filterIsInstance<Field>() }
  val referencedFields = componentDiff(listOf(oldJar), listOf(newJar)) { it.referencedMembers.filterIsInstance<Field>() }

  val changed = methods.changed || fields.changed

  override fun toTextReport(): DiffReport = JarDiffTextReport(this)
}

internal fun JarDiff.toSummaryTable() = diffuseTable {
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

    fun addDexRow(name: String, diff: ComponentDiff<*>) = row {
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

    // TODO addDexRow("strings", strings)
    // TODO addDexRow("types", types)
    // TODO addDexRow("classes", classes)
    addDexRow("methods", methods)
    addDexRow("fields", fields)
  }
}.renderText()

internal fun JarDiff.toDetailReport() = buildString {
  // TODO appendComponentDiff("STRINGS", strings)
  // TODO appendComponentDiff("TYPES", types)
  appendComponentDiff("METHODS", methods)
  appendComponentDiff("FIELDS", fields)
}

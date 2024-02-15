package com.jakewharton.diffuse.diff

import com.jakewharton.diffuse.diffuseTable
import com.jakewharton.diffuse.format.Dex
import com.jakewharton.diffuse.format.Field
import com.jakewharton.diffuse.format.Method
import com.jakewharton.diffuse.report.Report
import com.jakewharton.diffuse.report.text.DexDiffTextReport
import com.jakewharton.diffuse.report.toDiffString
import com.jakewharton.picnic.TextAlignment.BottomCenter
import com.jakewharton.picnic.TextAlignment.BottomLeft
import com.jakewharton.picnic.TextAlignment.MiddleLeft
import com.jakewharton.picnic.TextAlignment.MiddleRight
import com.jakewharton.picnic.renderText

internal class DexDiff(
  val oldDexes: List<Dex>,
  val newDexes: List<Dex>,
) : BinaryDiff {
  val isMultidex = oldDexes.size > 1 || newDexes.size > 1

  val strings = componentDiff(oldDexes, newDexes) { it.strings }
  val types = componentDiff(oldDexes, newDexes) { it.types }
  val classes = componentDiff(oldDexes, newDexes) { it.classes }
  val methods = componentDiff(oldDexes, newDexes) { it.members.filterIsInstance<Method>() }
  val declaredMethods = componentDiff(oldDexes, newDexes) { it.declaredMembers.filterIsInstance<Method>() }
  val referencedMethods = componentDiff(oldDexes, newDexes) { it.referencedMembers.filterIsInstance<Method>() }
  val fields = componentDiff(oldDexes, newDexes) { it.members.filterIsInstance<Field>() }
  val declaredFields = componentDiff(oldDexes, newDexes) { it.declaredMembers.filterIsInstance<Field>() }
  val referencedFields = componentDiff(oldDexes, newDexes) { it.referencedMembers.filterIsInstance<Field>() }

  val changed = strings.changed || types.changed || methods.changed || fields.changed

  override fun toTextReport(): Report = DexDiffTextReport(this)
}

internal fun DexDiff.toSummaryTable() = diffuseTable {
  header {
    if (isMultidex) {
      row {
        cell("DEX") {
          rowSpan = 2
          alignment = BottomLeft
        }
        cell("raw") {
          columnSpan = 3
          alignment = BottomCenter
        }
        cell("unique") {
          columnSpan = 4
          alignment = BottomCenter
        }
      }
    }
    row {
      if (!isMultidex) {
        cell("DEX")
      } else {
        cell("old")
        cell("new")
        cell("diff")
      }
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

    row {
      cell("files")
      cell(oldDexes.size)
      cell(newDexes.size)
      cell((newDexes.size - oldDexes.size).toDiffString()) {
        if (!isMultidex) {
          borderRight = false
        }
      }
      if (isMultidex) {
        // Add empty cells to ensure borders get drawn
        cell("")
        cell("")
        cell("") {
          columnSpan = 2
        }
      }
    }

    fun addDexRow(name: String, diff: ComponentDiff<*>) = row {
      cell(name)
      if (isMultidex) {
        cell(diff.oldRawCount)
        cell(diff.newRawCount)
        cell((diff.newRawCount - diff.oldRawCount).toDiffString())
      }
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

    addDexRow("strings", strings)
    addDexRow("types", types)
    addDexRow("classes", classes)
    addDexRow("methods", methods)
    addDexRow("fields", fields)
  }
}.renderText()

internal fun DexDiff.toDetailReport() = buildString {
  appendComponentDiff("STRINGS", strings)
  appendComponentDiff("TYPES", types)
  appendComponentDiff("METHODS", methods)
  appendComponentDiff("FIELDS", fields)
}

package com.jakewharton.diffuse.diff

import com.jakewharton.dex.ApiMapping
import com.jakewharton.dex.DexField
import com.jakewharton.dex.DexMethod
import com.jakewharton.diffuse.Dex
import com.jakewharton.diffuse.diff.DexDiff.ComponentDiff
import com.jakewharton.diffuse.diffuseTable
import com.jakewharton.diffuse.report.toDiffString
import com.jakewharton.picnic.TextAlignment.BottomCenter
import com.jakewharton.picnic.TextAlignment.BottomLeft
import com.jakewharton.picnic.TextAlignment.MiddleLeft
import com.jakewharton.picnic.TextAlignment.MiddleRight
import com.jakewharton.picnic.renderText

internal class DexDiff(
  val oldDexes: List<Dex>,
  val oldMapping: ApiMapping,
  val newDexes: List<Dex>,
  val newMapping: ApiMapping
) {
  val isMultidex = oldDexes.size > 1 || newDexes.size > 1

  val strings = componentDiff { it.strings }
  val types = componentDiff { it.types }
  val classes = componentDiff { it.classes }
  val methods = componentDiff { it.members.filterIsInstance<DexMethod>() }
  val declaredMethods = componentDiff { it.declaredMembers.filterIsInstance<DexMethod>() }
  val referencedMethods = componentDiff { it.referencedMembers.filterIsInstance<DexMethod>() }
  val fields = componentDiff { it.members.filterIsInstance<DexField>() }
  val declaredFields = componentDiff { it.declaredMembers.filterIsInstance<DexField>() }
  val referencedFields = componentDiff { it.referencedMembers.filterIsInstance<DexField>() }

  val changed = strings.changed || types.changed || methods.changed || fields.changed

  private fun <T> componentDiff(selector: (Dex) -> Collection<T>): ComponentDiff<T> {
    val oldRawCount = oldDexes.sumBy { selector(it).size }
    val newRawCount = newDexes.sumBy { selector(it).size }
    val old = oldDexes.flatMapTo(mutableSetOf(), selector)
    val new = newDexes.flatMapTo(mutableSetOf(), selector)
    val added = new - old
    val removed = old - new
    return ComponentDiff(oldRawCount, old.size, newRawCount, new.size, added, removed)
  }

  class ComponentDiff<T>(
    val oldRawCount: Int,
    val oldCount: Int,
    val newRawCount: Int,
    val newCount: Int,
    val added: Set<T>,
    val removed: Set<T>
  ) {
    val changed get() = added.isNotEmpty() || removed.isNotEmpty()
  }
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
      cell("count")
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
  fun appendComponentDiff(name: String, diff: ComponentDiff<*>) {
    if (diff.changed) {
      appendln()
      appendln("$name:")
      appendln()
      appendln(buildString {
        appendln(diffuseTable {
          header {
            row {
              cell("old")
              cell("new")
              cell("diff")
            }
          }

          val diffSize = (diff.added.size - diff.removed.size).toDiffString()
          val addedSize = diff.added.size.toDiffString(zeroSign = '+')
          val removedSize = (-diff.removed.size).toDiffString(zeroSign = '-')
          row(diff.oldCount, diff.newCount, "$diffSize ($addedSize $removedSize)")
        }.renderText())
        diff.added.forEach {
          appendln("+ $it")
        }
        if (diff.added.isNotEmpty() && diff.removed.isNotEmpty()) {
          appendln()
        }
        diff.removed.forEach {
          appendln("- $it")
        }
      }.prependIndent("  "))
    }
  }

  appendComponentDiff("STRINGS", strings)
  appendComponentDiff("TYPES", types)
  appendComponentDiff("METHODS", methods)
  appendComponentDiff("FIELDS", fields)
}

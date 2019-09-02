package com.jakewharton.diffuse

import com.jakewharton.dex.ApiMapping
import com.jakewharton.dex.DexField
import com.jakewharton.dex.DexMethod
import com.jakewharton.picnic.TextAlignment.BottomCenter
import com.jakewharton.picnic.TextAlignment.BottomLeft
import com.jakewharton.picnic.TextAlignment.MiddleLeft
import com.jakewharton.picnic.TextAlignment.MiddleRight
import com.jakewharton.picnic.renderText

class DexDiff(
    val oldDexes: List<Dex>,
    val oldMapping: ApiMapping = ApiMapping.EMPTY,
    val newDexes: List<Dex>,
    val newMapping: ApiMapping = ApiMapping.EMPTY
) {
  /** True if either [oldDexes] or [newDexes] is larger than 1. */
  val isMultidex get() = oldDexes.size > 1 || newDexes.size > 1

  fun toTextReport(): String = diffuseTable {
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
        cell((newDexes.size - oldDexes.size).toDiffString())
        if (isMultidex) {
          // Add empty cells to ensure borders get drawn
          cell("")
          cell("")
          cell("") {
            columnSpan = 2
          }
        }
      }

      fun addDexRow(name: String, selector: (Dex) -> Collection<Any>) = row {
        cell(name)

        if (isMultidex) {
          val oldSize = oldDexes.sumBy { selector(it).size }
          val newSize = newDexes.sumBy { selector(it).size }
          val diff = (newSize - oldSize).toDiffString()
          cell(oldSize)
          cell(newSize)
          cell(diff)
        }

        val oldUnique = oldDexes.flatMapTo(mutableSetOf(), selector)
        cell(oldUnique.size)

        val newUnique = newDexes.flatMapTo(mutableSetOf(), selector)
        cell(newUnique.size)

        val added = newUnique - oldUnique
        val removed = oldUnique - newUnique
        val addedSize = added.size.toDiffString()
        val removedSize = (-removed.size).toDiffString()
        val finalDiff = (added.size - removed.size).toDiffString()
        cell(finalDiff) {
          borderRight = false
        }
        cell("($addedSize $removedSize)") {
          borderLeft = false
          paddingLeft = 0
          alignment = MiddleLeft
        }
      }

      addDexRow("strings") { it.strings }
      addDexRow("types") { it.types }
      addDexRow("classes") { it.classes }
      addDexRow("methods") { it.allMembers.filterIsInstance<DexMethod>() }
      addDexRow("fields") { it.allMembers.filterIsInstance<DexField>() }
    }
  }.renderText()
}

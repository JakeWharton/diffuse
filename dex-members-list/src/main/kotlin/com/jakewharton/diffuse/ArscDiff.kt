package com.jakewharton.diffuse

import com.jakewharton.picnic.TextAlignment.MiddleLeft
import com.jakewharton.picnic.TextAlignment.MiddleRight
import com.jakewharton.picnic.renderText

class ArscDiff(
  val oldArsc: Arsc,
  val newArsc: Arsc
) {
  fun toTextReport() = diffuseTable {
    header {
      row {
        cell("ARSC")
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
        cell("configs")

        val oldConfigs = oldArsc.configs
        cell(oldConfigs.size)

        val newConfigs = newArsc.configs
        cell(newConfigs.size)

        val configsAdded = newConfigs - oldConfigs
        val configsRemoved = oldConfigs - newConfigs
        val configsDelta = configsAdded.size - configsRemoved.size
        cell(configsDelta.toDiffString()) {
          borderRight = false
        }

        val delta = if (configsDelta > 0) {
          val added = configsAdded.size.toDiffString()
          val removed = (-configsRemoved.size).toDiffString()
          "($added $removed)"
        } else {
          ""
        }
        cell(delta) {
          borderLeft = false
          paddingLeft = 0
          alignment = MiddleLeft
        }
      }

      row {
        cell("entries")

        val oldEntries = oldArsc.entries
        cell(oldEntries.size)

        val newEntries = newArsc.entries
        cell(newEntries.size)

        val entriesAdded = newEntries - oldEntries
        val entriesRemoved = oldEntries - newEntries
        val entriesDelta = entriesAdded.size - entriesRemoved.size
        cell(entriesDelta.toDiffString()) {
          borderRight = false
        }

        val delta = if (entriesDelta > 0) {
          val added = entriesAdded.size.toDiffString()
          val removed = (-entriesRemoved.size).toDiffString()
          "($added $removed)"
        } else {
          ""
        }
        cell(delta) {
          borderLeft = false
          paddingLeft = 0
          alignment = MiddleLeft
        }
      }
    }
  }.renderText()
}

package com.jakewharton.diffuse.diff

import com.jakewharton.diffuse.diffuseTable
import com.jakewharton.diffuse.format.Arsc
import com.jakewharton.diffuse.report.toDiffString
import com.jakewharton.picnic.TextAlignment.MiddleLeft
import com.jakewharton.picnic.TextAlignment.MiddleRight
import com.jakewharton.picnic.renderText
import kotlinx.html.FlowContent
import kotlinx.html.br
import kotlinx.html.div
import kotlinx.html.p
import kotlinx.html.span
import kotlinx.html.style
import kotlinx.html.table
import kotlinx.html.tbody
import kotlinx.html.td
import kotlinx.html.thead
import kotlinx.html.tr

internal class ArscDiff(
  val oldArsc: Arsc,
  val newArsc: Arsc,
) {
  val configsAdded = (newArsc.configs - oldArsc.configs).sorted()
  val configsRemoved = (oldArsc.configs - newArsc.configs).sorted()
  val entriesAdded = (newArsc.entries.values - oldArsc.entries.values).sorted()
  val entriesRemoved = (oldArsc.entries.values - newArsc.entries.values).sorted()

  val changed = configsAdded.isNotEmpty() ||
    configsRemoved.isNotEmpty() ||
    entriesAdded.isNotEmpty() ||
    entriesRemoved.isNotEmpty()
}

internal fun ArscDiff.toSummaryTable() = diffuseTable {
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
      cell(oldArsc.configs.size)
      cell(newArsc.configs.size)

      val configsDelta = configsAdded.size - configsRemoved.size
      cell(configsDelta.toDiffString()) {
        borderRight = false
      }

      val delta = if (configsAdded.isNotEmpty() || configsRemoved.isNotEmpty()) {
        val added = configsAdded.size.toDiffString(zeroSign = '+')
        val removed = (-configsRemoved.size).toDiffString(zeroSign = '-')
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
      cell(oldArsc.entries.size)
      cell(newArsc.entries.size)

      val entriesDelta = entriesAdded.size - entriesRemoved.size
      cell(entriesDelta.toDiffString()) {
        borderRight = false
      }

      val delta = if (entriesAdded.isNotEmpty() || entriesRemoved.isNotEmpty()) {
        val added = entriesAdded.size.toDiffString(zeroSign = '+')
        val removed = (-entriesRemoved.size).toDiffString(zeroSign = '-')
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

internal fun ArscDiff.toDetailReport() = buildString {
  fun <T> appendComponentDiff(
    name: String,
    componentSelector: (Arsc) -> Collection<*>,
    added: List<T>,
    removed: List<T>,
  ) {
    if (added.isNotEmpty() || removed.isNotEmpty()) {
      appendLine()
      appendLine("$name:")
      appendLine()
      appendLine(
        buildString {
          appendLine(
            diffuseTable {
              header {
                row {
                  cell("old")
                  cell("new")
                  cell("diff")
                }
              }

              val diffSize = (added.size - removed.size).toDiffString()
              val addedSize = added.size.toDiffString(zeroSign = '+')
              val removedSize = (-removed.size).toDiffString(zeroSign = '-')
              row(
                componentSelector(oldArsc).size,
                componentSelector(newArsc).size,
                "$diffSize ($addedSize $removedSize)",
              )
            }.renderText(),
          )
          added.forEach {
            appendLine("+ $it")
          }
          if (added.isNotEmpty() && removed.isNotEmpty()) {
            appendLine()
          }
          removed.forEach {
            appendLine("- $it")
          }
        }.prependIndent("  "),
      )
    }
  }

  appendComponentDiff("CONFIGS", Arsc::configs, configsAdded, configsRemoved)
  appendComponentDiff("ENTRIES", { it.entries.values }, entriesAdded, entriesRemoved)
}

internal fun FlowContent.toSummaryTable(diff: ArscDiff) {
  table {
    thead {
      tr {
        td { +"ARSC" }
        td { +"old" }
        td { +"new" }
        td { colSpan = "2" + "diff" }
      }
    }

    tbody {
      style = "text-align: right; vertical-align: center;"

      tr {
        td { +"configs" }
        td { +diff.oldArsc.configs.size }
        td { +diff.newArsc.configs.size }

        val configsDelta = diff.configsAdded.size - diff.configsRemoved.size
        td {
          style = "border-right: none;"
          +configsDelta.toDiffString()
        }

        val delta = if (diff.configsAdded.isNotEmpty() || diff.configsRemoved.isNotEmpty()) {
          val added = diff.configsAdded.size.toDiffString(zeroSign = '+')
          val removed = (-diff.configsRemoved.size).toDiffString(zeroSign = '-')
          "($added $removed)"
        } else {
          ""
        }

        td {
          style = "border-left: none; padding-left: 0; text-align: left; vertical-align: center;"
          +delta
        }
      }

      tr {
        td { +"entries" }
        td { +diff.oldArsc.entries.size }
        td { +diff.newArsc.entries.size }

        val entriesDelta = diff.entriesAdded.size - diff.entriesRemoved.size
        td {
          style = "border-right: none;"
          +entriesDelta.toDiffString()
        }

        val delta = if (diff.entriesAdded.isNotEmpty() || diff.entriesRemoved.isNotEmpty()) {
          val added = diff.entriesAdded.size.toDiffString(zeroSign = '+')
          val removed = (-diff.entriesRemoved.size).toDiffString(zeroSign = '-')
          "($added $removed)"
        } else {
          ""
        }

        td {
          style = "border-left: none; padding-left: 0; text-align: left; vertical-align: center;"
          +delta
        }
      }
    }
  }
}

internal fun FlowContent.toDetailReport(diff: ArscDiff) {
  fun FlowContent.appendComponentDiff(
    name: String,
    diff: ArscDiff,
    componentSelector: (Arsc) -> Collection<*>,
  ) {
    if (diff.configsAdded.isNotEmpty() || diff.configsRemoved.isNotEmpty()) {
      p { +"$name:" }

      div {
        style = "margin-left: 16pt;"

        table {
          thead {
            tr {
              td { +"old" }
              td { +"new" }
              td { +"diff" }
            }
          }
          tbody {
            val diffSize = (diff.configsAdded.size - diff.configsRemoved.size).toDiffString()
            val addedSize = diff.configsAdded.size.toDiffString(zeroSign = '+')
            val removedSize = (-diff.configsRemoved.size).toDiffString(zeroSign = '-')
            tr {
              td { +componentSelector(diff.oldArsc).size }
              td { +componentSelector(diff.newArsc).size }
              td { +"$diffSize ($addedSize $removedSize)" }
            }
          }
        }

        diff.configsAdded.forEach {
          span { +"+ $it" }
          br()
        }
        if (diff.configsAdded.isNotEmpty() && diff.configsRemoved.isNotEmpty()) {
          br()
          br()
        }
        diff.configsRemoved.forEach {
          span { +"- $it" }
          br()
        }
      }
    }
  }

  appendComponentDiff("CONFIGS", diff, Arsc::configs)
  appendComponentDiff("ENTRIES", diff) { it.entries.values }
}

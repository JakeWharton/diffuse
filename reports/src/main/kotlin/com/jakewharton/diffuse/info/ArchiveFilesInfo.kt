package com.jakewharton.diffuse.info

import com.jakewharton.diffuse.diffuseTable
import com.jakewharton.diffuse.format.ArchiveFile
import com.jakewharton.diffuse.format.ArchiveFiles
import com.jakewharton.diffuse.io.Size
import com.jakewharton.picnic.TableSectionDsl
import com.jakewharton.picnic.TextAlignment
import com.jakewharton.picnic.renderText
import kotlinx.html.FlowContent
import kotlinx.html.TR
import kotlinx.html.style
import kotlinx.html.table
import kotlinx.html.tbody
import kotlinx.html.td
import kotlinx.html.tfoot
import kotlinx.html.th
import kotlinx.html.thead
import kotlinx.html.tr

internal fun ArchiveFiles.toSummaryTable(
  name: String,
  displayTypes: List<ArchiveFile.Type>,
  skipIfEmptyTypes: Set<ArchiveFile.Type> = emptySet(),
  includeCompressed: Boolean = true,
) = diffuseTable {
  header {
    if (includeCompressed) {
      row {
        cell(name) {
          alignment = TextAlignment.BottomLeft
        }
        cell("compressed") {
          alignment = TextAlignment.BottomCenter
        }
        cell("uncompressed") {
          alignment = TextAlignment.BottomCenter
        }
      }
    } else {
      row {
        cell(name) {
          alignment = TextAlignment.BottomLeft
        }
        cell("size")
      }
    }
  }

  fun TableSectionDsl.addArchiveFileRow(name: String, type: ArchiveFile.Type? = null) {
    val old = if (type != null) filterValues { it.type == type } else this@toSummaryTable
    val oldSize = old.values.fold(Size.ZERO) { acc, file -> acc + file.size }
    val oldUncompressedSize = old.values.fold(Size.ZERO) { acc, file -> acc + file.uncompressedSize }
    if (oldSize != Size.ZERO || type !in skipIfEmptyTypes) {
      if (includeCompressed) {
        row(
          name,
          oldSize,
          oldUncompressedSize,
        )
      } else {
        row(name, oldUncompressedSize)
      }
    }
  }

  body {
    cellStyle {
      alignment = TextAlignment.MiddleRight
    }
    for (type in displayTypes) {
      addArchiveFileRow(type.displayName, type)
    }
  }

  footer {
    cellStyle {
      alignment = TextAlignment.MiddleRight
    }
    addArchiveFileRow("total")
  }
}.renderText()

internal fun FlowContent.toSummaryTable(
  name: String,
  files: ArchiveFiles,
  displayTypes: List<ArchiveFile.Type>,
  skipIfEmptyTypes: Set<ArchiveFile.Type> = emptySet(),
  includeCompressed: Boolean = true,
) {
  table {
    thead {
      tr {
        if (includeCompressed) {
          th {
            style = "text-align: left; vertical-align: bottom;"

            +name
          }

          th {
            style = "text-align: center; vertical-align: bottom;"

            +"compressed"
          }

          th {
            style = "text-align: center; vertical-align: bottom;"

            +"uncompressed"
          }
        } else {
          th {
            style = "text-align: left; vertical-align: bottom;"

            +name
          }

          th {
            +"size"
          }
        }
      }
    }

    fun TR.addArchiveFileRow(name: String, type: ArchiveFile.Type? = null) {
      val old = if (type != null) files.filterValues { it.type == type } else files
      val oldSize = old.values.fold(Size.ZERO) { acc, file -> acc + file.size }
      val oldUncompressedSize = old.values.fold(Size.ZERO) { acc, file -> acc + file.uncompressedSize }
      if (oldSize != Size.ZERO || type !in skipIfEmptyTypes) {
        if (includeCompressed) {
          td { +name }
          td { +oldSize.toString() }
          td { +oldUncompressedSize.toString() }
        } else {
          td { +name }
          td { +oldUncompressedSize.toString() }
        }
      }
    }

    tbody {
      style = "text-align: right; vertical-align: middle;"

      for (type in displayTypes) {
        tr {
          addArchiveFileRow(type.displayName, type)
        }
      }
    }

    tfoot {
      tr {
        style = "text-align: right; vertical-align: middle;"
        addArchiveFileRow("total")
      }
    }
  }
}

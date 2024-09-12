package com.jakewharton.diffuse.diff

import com.jakewharton.diffuse.diff.ArchiveFilesDiff.Change
import com.jakewharton.diffuse.diffuseTable
import com.jakewharton.diffuse.format.ArchiveFile.Type
import com.jakewharton.diffuse.format.ArchiveFiles
import com.jakewharton.diffuse.io.Size
import com.jakewharton.diffuse.report.htmlEncoded
import com.jakewharton.diffuse.report.toDiffString
import com.jakewharton.picnic.TableSectionDsl
import com.jakewharton.picnic.TextAlignment.BottomCenter
import com.jakewharton.picnic.TextAlignment.BottomLeft
import com.jakewharton.picnic.TextAlignment.MiddleCenter
import com.jakewharton.picnic.TextAlignment.MiddleRight
import com.jakewharton.picnic.renderText
import kotlinx.html.FlowContent
import kotlinx.html.TR
import kotlinx.html.style
import kotlinx.html.table
import kotlinx.html.tbody
import kotlinx.html.td
import kotlinx.html.tfoot
import kotlinx.html.thead
import kotlinx.html.tr
import kotlinx.html.unsafe

internal class ArchiveFilesDiff(
  val oldFiles: ArchiveFiles,
  val newFiles: ArchiveFiles,
  val includeCompressed: Boolean = true,
) {
  data class Change(
    val path: String,
    val size: Size,
    val sizeDiff: Size,
    val uncompressedSize: Size,
    val uncompressedSizeDiff: Size,
    val type: Type,
  ) {
    enum class Type { Added, Removed, Changed }
  }

  val changes: List<Change> = run {
    val added = newFiles.mapNotNull { (path, newFile) ->
      if (path !in oldFiles) {
        Change(
          path,
          newFile.size,
          newFile.size,
          newFile.uncompressedSize,
          newFile.uncompressedSize,
          Change.Type.Added,
        )
      } else {
        null
      }
    }
    val removed = oldFiles.mapNotNull { (path, oldFile) ->
      if (path !in newFiles) {
        Change(
          path,
          Size.ZERO,
          -oldFile.size,
          Size.ZERO,
          -oldFile.uncompressedSize,
          Change.Type.Removed,
        )
      } else {
        null
      }
    }
    val changed = oldFiles.mapNotNull { (path, oldFile) ->
      val newFile = newFiles[path]?.let {
        if (includeCompressed) {
          it
        } else {
          it.copy(size = oldFile.size, isCompressed = oldFile.isCompressed)
        }
      }
      if (newFile != null && newFile != oldFile) {
        Change(
          path,
          newFile.size,
          newFile.size - oldFile.size,
          newFile.uncompressedSize,
          newFile.uncompressedSize - oldFile.uncompressedSize,
          Change.Type.Changed,
        )
      } else {
        null
      }
    }

    (added + removed + changed).sortedByDescending { it.sizeDiff.absoluteValue }
  }

  val changed = oldFiles != newFiles
}

internal fun ArchiveFilesDiff.toSummaryTable(
  name: String,
  displayTypes: List<Type>,
  skipIfEmptyTypes: Set<Type> = emptySet(),
) = diffuseTable {
  header {
    if (includeCompressed) {
      row {
        cell(name) {
          alignment = BottomLeft
          rowSpan = 2
        }
        cell("compressed") {
          columnSpan = 3
          alignment = BottomCenter
        }
        cell("uncompressed") {
          columnSpan = 3
          alignment = BottomCenter
        }
      }
      row("old", "new", "diff", "old", "new", "diff")
    } else {
      row {
        cell(name) {
          alignment = BottomLeft
        }
        cell("old")
        cell("new")
        cell("diff")
      }
    }
  }

  fun TableSectionDsl.addArchiveRow(name: String, type: Type? = null) {
    val old = if (type != null) oldFiles.filterValues { it.type == type } else oldFiles
    val new = if (type != null) newFiles.filterValues { it.type == type } else newFiles
    val oldSize = old.values.fold(Size.ZERO) { acc, file -> acc + file.size }
    val newSize = new.values.fold(Size.ZERO) { acc, file -> acc + file.size }
    val oldUncompressedSize = old.values.fold(Size.ZERO) { acc, file -> acc + file.uncompressedSize }
    val newUncompressedSize = new.values.fold(Size.ZERO) { acc, file -> acc + file.uncompressedSize }
    if (oldSize != Size.ZERO || newSize != Size.ZERO || type !in skipIfEmptyTypes) {
      val uncompressedDiff = (newUncompressedSize - oldUncompressedSize).toDiffString()
      if (includeCompressed) {
        row(
          name,
          oldSize,
          newSize,
          (newSize - oldSize).toDiffString(),
          oldUncompressedSize,
          newUncompressedSize,
          uncompressedDiff,
        )
      } else {
        row(name, oldUncompressedSize, newUncompressedSize, uncompressedDiff)
      }
    }
  }

  body {
    cellStyle {
      alignment = MiddleRight
    }
    for (type in displayTypes) {
      addArchiveRow(type.displayName, type)
    }
  }

  footer {
    cellStyle {
      alignment = MiddleRight
    }
    addArchiveRow("total")
  }
}.renderText()

internal fun ArchiveFilesDiff.toDetailReport() = buildString {
  appendLine()
  appendLine(
    diffuseTable {
      header {
        if (includeCompressed) {
          row {
            cell("compressed") {
              columnSpan = 2
              alignment = MiddleCenter
            }
            cell("uncompressed") {
              columnSpan = 2
              alignment = MiddleCenter
            }
            cell("path") {
              rowSpan = 2
              alignment = BottomLeft
            }
          }
          row("size", "diff", "size", "diff")
        } else {
          row {
            cell("size")
            cell("diff")
            cell("path") {
              alignment = BottomLeft
            }
          }
        }
      }
      footer {
        row {
          if (includeCompressed) {
            val totalSize = changes.fold(Size.ZERO) { acc, change -> acc + change.size }
            val totalDiff = changes.fold(Size.ZERO) { acc, change -> acc + change.sizeDiff }
            cell(totalSize) {
              alignment = MiddleRight
            }
            cell(totalDiff.toDiffString()) {
              alignment = MiddleRight
            }
          }
          val totalUncompressedSize = changes.fold(Size.ZERO) { acc, change -> acc + change.uncompressedSize }
          val totalUncompressedDiff = changes.fold(Size.ZERO) { acc, change -> acc + change.uncompressedSizeDiff }
          cell(totalUncompressedSize) {
            alignment = MiddleRight
          }
          cell(totalUncompressedDiff.toDiffString()) {
            alignment = MiddleRight
          }
          cell("(total)")
        }
      }
      for ((path, size, sizeDiff, uncompressedSize, uncompressedSizeDiff, type) in changes) {
        val typeChar = when (type) {
          Change.Type.Added -> '+'
          Change.Type.Removed -> '-'
          Change.Type.Changed -> '∆'
        }
        row {
          if (includeCompressed) {
            cell(if (type != Change.Type.Removed) size else "") {
              alignment = MiddleRight
            }
            cell(sizeDiff.toDiffString()) {
              alignment = MiddleRight
            }
          }
          cell(if (type != Change.Type.Removed) uncompressedSize else "") {
            alignment = MiddleRight
          }
          cell(uncompressedSizeDiff.toDiffString()) {
            alignment = MiddleRight
          }
          cell("$typeChar $path")
        }
      }
    }.renderText(),
  )
}

internal fun FlowContent.toSummaryTable(
  name: String,
  diff: ArchiveFilesDiff,
  displayTypes: List<Type>,
  skipIfEmptyTypes: Set<Type> = emptySet(),
) {
  table {
    thead {
      if (diff.includeCompressed) {
        tr {
          td {
            rowSpan = "2"
            style = "text-align: left; vertical-align: bottom;"
            +name
          }
          td {
            colSpan = "3"
            style = "text-align: center; vertical-align: bottom;"
            +"compressed"
          }
          td {
            colSpan = "3"
            style = "text-align: center; vertical-align: bottom;"
            +"uncompressed"
          }
        }
        tr {
          td { +"old" }
          td { +"new" }
          td { +"diff" }
          td { +"old" }
          td { +"new" }
          td { +"diff" }
        }
      } else {
        tr {
          td {
            style = "text-align: left; vertical-align: bottom;"
            +name
          }
          td { +"old" }
          td { +"new" }
          td { +"diff" }
        }
      }
    }

    fun TR.addArchiveRow(name: String, type: Type? = null) {
      val old = if (type != null) diff.oldFiles.filterValues { it.type == type } else diff.oldFiles
      val new = if (type != null) diff.newFiles.filterValues { it.type == type } else diff.newFiles
      val oldSize = old.values.fold(Size.ZERO) { acc, file -> acc + file.size }
      val newSize = new.values.fold(Size.ZERO) { acc, file -> acc + file.size }
      val oldUncompressedSize = old.values.fold(Size.ZERO) { acc, file -> acc + file.uncompressedSize }
      val newUncompressedSize = new.values.fold(Size.ZERO) { acc, file -> acc + file.uncompressedSize }
      if (oldSize != Size.ZERO || newSize != Size.ZERO || type !in skipIfEmptyTypes) {
        val uncompressedDiff = (newUncompressedSize - oldUncompressedSize).toDiffString()
        if (diff.includeCompressed) {
          td { +name }
          td { +oldSize.toString() }
          td { +newSize.toString() }
          td { +(newSize - oldSize).toString() }
          td { +oldUncompressedSize.toString() }
          td { +newUncompressedSize.toString() }
          td { +uncompressedDiff }
        } else {
          td { +name }
          td { +oldUncompressedSize.toString() }
          td { +newUncompressedSize.toString() }
          td { +uncompressedDiff }
        }
      }
    }

    tbody {
      style = "text-align: right; vertical-align: middle;"

      for (type in displayTypes) {
        tr {
          addArchiveRow(type.displayName, type)
        }
      }
    }

    tfoot {
      style = "text-align: right; vertical-align: middle;"
      tr { addArchiveRow("total") }
    }
  }
}

internal fun FlowContent.toDetailReport(diff: ArchiveFilesDiff) {
  table {
    thead {
      if (diff.includeCompressed) {
        tr {
          td {
            style = "text-align: center; vertical-align: middle;"
            colSpan = "2"
            +"compressed"
          }
          td {
            style = "text-align: center; vertical-align: middle;"
            colSpan = "2"
            +"uncompressed"
          }

          td {
            style = "text-align: left; vertical-align: bottom;"
            rowSpan = "2"
            +"path"
          }
        }
        tr {
          td { +"size" }
          td { +"diff" }
          td { +"size" }
          td { +"diff" }
        }
      } else {
        tr {
          td { +"size" }
          td { +"diff" }
          td {
            style = "text-align: left; vertical-align: bottom;"
            +"path"
          }
        }
      }
    }
    tfoot {
      tr {
        if (diff.includeCompressed) {
          val totalSize = diff.changes.fold(Size.ZERO) { acc, change -> acc + change.size }
          val totalDiff = diff.changes.fold(Size.ZERO) { acc, change -> acc + change.sizeDiff }
          td {
            style = "text-align: right; vertical-align: middle;"
            +totalSize.toString()
          }
          td {
            style = "text-align: right; vertical-align: middle;"
            +totalDiff.toDiffString()
          }
        }
        val totalUncompressedSize = diff.changes.fold(Size.ZERO) { acc, change -> acc + change.uncompressedSize }
        val totalUncompressedDiff = diff.changes.fold(Size.ZERO) { acc, change -> acc + change.uncompressedSizeDiff }
        td {
          style = "text-align: right; vertical-align: middle;"
          +totalUncompressedSize.toString()
        }
        td {
          style = "text-align: right; vertical-align: middle;"
          +totalUncompressedDiff.toDiffString()
        }
        td { +"(total)" }
      }
    }
    for ((path, size, sizeDiff, uncompressedSize, uncompressedSizeDiff, type) in diff.changes) {
      val typeChar = when (type) {
        Change.Type.Added -> '+'
        Change.Type.Removed -> '-'
        Change.Type.Changed -> '∆'
      }
      tr {
        if (diff.includeCompressed) {
          td {
            style = "text-align: right; vertical-align: middle;"
            if (type != Change.Type.Removed) +size.toString() else +""
          }
          td {
            style = "text-align: right; vertical-align: middle;"
            +sizeDiff.toDiffString()
          }
        }
        td {
          style = "text-align: right; vertical-align: middle;"
          if (type != Change.Type.Removed) +uncompressedSize.toString() else +""
        }
        td {
          style = "text-align: right; vertical-align: middle;"
          +uncompressedSizeDiff.toDiffString()
        }
        td { unsafe { raw("$typeChar $path".htmlEncoded) } }
      }
    }
  }
}

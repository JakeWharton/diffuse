package com.jakewharton.diffuse

import com.jakewharton.diffuse.ArchiveFile.Type
import com.jakewharton.diffuse.ArchiveFilesDiff.Change
import com.jakewharton.picnic.SectionDsl
import com.jakewharton.picnic.TextAlignment.BottomCenter
import com.jakewharton.picnic.TextAlignment.BottomLeft
import com.jakewharton.picnic.TextAlignment.MiddleCenter
import com.jakewharton.picnic.TextAlignment.MiddleRight
import com.jakewharton.picnic.renderText

internal class ArchiveFilesDiff(
  val oldFiles: ArchiveFiles,
  val newFiles: ArchiveFiles
) {
  data class Change(
    val path: String,
    val size: Size,
    val sizeDiff: Size,
    val uncompressedSize: Size,
    val uncompressedSizeDiff: Size,
    val type: Type
  ) {
    enum class Type { Added, Removed, Changed }
  }

  val changes: List<Change>
  init {
    val added = newFiles.mapNotNull { (path, newFile) ->
      if (path !in oldFiles) {
        Change(path, newFile.size, newFile.size, newFile.uncompressedSize, newFile.uncompressedSize,
            Change.Type.Added)
      } else {
        null
      }
    }
    val removed = oldFiles.mapNotNull { (path, oldFile) ->
      if (path !in newFiles) {
        Change(path, Size.ZERO, -oldFile.size, Size.ZERO, -oldFile.uncompressedSize,
            Change.Type.Removed)
      } else {
        null
      }
    }
    val changed = oldFiles.mapNotNull { (path, oldFile) ->
      val newFile = newFiles[path]
      if (newFile != null && newFile != oldFile) {
        Change(path, newFile.size, newFile.size - oldFile.size,
            newFile.uncompressedSize, newFile.uncompressedSize - oldFile.uncompressedSize,
            Change.Type.Changed)
      } else {
        null
      }
    }
    changes = (added + removed + changed).sortedByDescending { it.sizeDiff.absoluteValue }
  }

  val changed = oldFiles != newFiles
}

internal fun ArchiveFilesDiff.toSummaryTable(
  name: String,
  displayTypes: List<Type>,
  skipIfEmptyTypes: Set<Type> = emptySet()
) = diffuseTable {
  header {
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
  }

  fun SectionDsl.addApkRow(name: String, type: Type? = null) {
    val old = if (type != null) oldFiles.filterValues { it.type == type } else oldFiles
    val new = if (type != null) newFiles.filterValues { it.type == type } else newFiles
    val oldSize = old.values.fold(Size.ZERO) { acc, file -> acc + file.size }
    val newSize = new.values.fold(Size.ZERO) { acc, file -> acc + file.size }
    val oldUncompressedSize = old.values.fold(Size.ZERO) { acc, file -> acc + file.uncompressedSize }
    val newUncompressedSize = new.values.fold(Size.ZERO) { acc, file -> acc + file.uncompressedSize }
    if (oldSize != Size.ZERO || newSize != Size.ZERO || type !in skipIfEmptyTypes) {
      row(name, oldSize, newSize, (newSize - oldSize).toDiffString(), oldUncompressedSize,
          newUncompressedSize, (newUncompressedSize - oldUncompressedSize).toDiffString())
    }
  }

  body {
    cellStyle {
      alignment = MiddleRight
    }
    for (type in displayTypes) {
      addApkRow(type.displayName, type)
    }
  }

  footer {
    cellStyle {
      alignment = MiddleRight
    }
    addApkRow("total")
  }
}.renderText()

internal fun ArchiveFilesDiff.toDetailReport() = buildString {
  appendln()
  appendln(diffuseTable {
    header {
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
    }
    footer {
      val totalSize = changes.fold(Size.ZERO) { acc, change -> acc + change.size }
      val totalDiff = changes.fold(Size.ZERO) { acc, change -> acc + change.sizeDiff }
      val totalUncompressedSize = changes.fold(Size.ZERO) { acc, change -> acc + change.uncompressedSize }
      val totalUncompressedDiff = changes.fold(Size.ZERO) { acc, change -> acc + change.uncompressedSizeDiff }
      row {
        cell(totalSize) {
          alignment = MiddleRight
        }
        cell(totalDiff.toDiffString()) {
          alignment = MiddleRight
        }
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
        cell(if (type != Change.Type.Removed) size else "") {
          alignment = MiddleRight
        }
        cell(sizeDiff.toDiffString()) {
          alignment = MiddleRight
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
  }.renderText())
}

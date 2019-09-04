package com.jakewharton.diffuse

import com.jakewharton.diffuse.ArchiveDiff.Change
import com.jakewharton.diffuse.ArchiveFile.Type
import com.jakewharton.picnic.SectionDsl
import com.jakewharton.picnic.TextAlignment.BottomCenter
import com.jakewharton.picnic.TextAlignment.BottomLeft
import com.jakewharton.picnic.TextAlignment.MiddleRight
import com.jakewharton.picnic.renderText
import java.util.SortedMap

internal class ArchiveDiff(
  val oldFiles: SortedMap<String, ArchiveFile>,
  val newFiles: SortedMap<String, ArchiveFile>
) {
  data class Change(
    val path: String,
    val sizeDiff: Size,
    val uncompressedSizeDiff: Size,
    val type: Type
  ) {
    enum class Type { Added, Removed, Changed }
  }

  val changes: List<Change>
  init {
    val added = newFiles.mapNotNull { (path, newFile) ->
      if (path !in oldFiles) {
        Change(path, newFile.size, newFile.uncompressedSize, Change.Type.Added)
      } else {
        null
      }
    }
    val removed = oldFiles.mapNotNull { (path, oldFile) ->
      if (path !in newFiles) {
        Change(path, -oldFile.size, -oldFile.uncompressedSize, Change.Type.Removed)
      } else {
        null
      }
    }
    val changed = oldFiles.mapNotNull { (path, oldFile) ->
      val newFile = newFiles[path]
      if (newFile != null && newFile != oldFile) {
        Change(path, newFile.size - oldFile.size,
            newFile.uncompressedSize - oldFile.uncompressedSize, Change.Type.Changed)
      } else {
        null
      }
    }
    changes = (added + removed + changed).sortedByDescending { it.sizeDiff.absoluteValue }
  }

  val changed = oldFiles != newFiles
}

internal fun ArchiveDiff.toSummaryTable(name: String) = diffuseTable {
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
    row(name, oldSize, newSize, (newSize - oldSize).toDiffString(), oldUncompressedSize,
        newUncompressedSize, (newUncompressedSize - oldUncompressedSize).toDiffString())
  }

  body {
    cellStyle {
      alignment = MiddleRight
    }

    addApkRow("dex", Type.Dex)
    addApkRow("arsc", Type.Arsc)
    addApkRow("manifest", Type.Manifest)
    addApkRow("res", Type.Res)
    addApkRow("native", Type.Native)
    addApkRow("asset", Type.Asset)
    addApkRow("other", Type.Other)
  }

  footer {
    cellStyle {
      alignment = MiddleRight
    }
    addApkRow("total")
  }
}.renderText()

internal fun ArchiveDiff.toDetailReport() = buildString {
  appendln()
  appendln(diffuseTable {
    header {
      row("diff", "diff (u)", "path")
    }
    footer {
      val totalDiff = changes.fold(Size.ZERO) { acc, change -> acc + change.sizeDiff }
      val totalUncompressedDiff = changes.fold(Size.ZERO) { acc, change -> acc + change.uncompressedSizeDiff }
      row {
        cell(totalDiff.toDiffString()) {
          alignment = MiddleRight
        }
        cell(totalUncompressedDiff.toDiffString()) {
          alignment = MiddleRight
        }
        cell("(total)")
      }
    }
    for ((path, sizeDiff, uncompressedSizeDiff, type) in changes) {
      val typeChar = when (type) {
        Change.Type.Added -> '+'
        Change.Type.Removed -> '-'
        Change.Type.Changed -> '∆'
      }
      row {
        cell(sizeDiff.toDiffString()) {
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

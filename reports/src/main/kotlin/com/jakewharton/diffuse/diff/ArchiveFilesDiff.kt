package com.jakewharton.diffuse.diff

import com.jakewharton.diffuse.diff.ArchiveFilesDiff.Change
import com.jakewharton.diffuse.diffuseTable
import com.jakewharton.diffuse.format.ArchiveFile.Type
import com.jakewharton.diffuse.format.ArchiveFiles
import com.jakewharton.diffuse.report.toDiffString
import com.jakewharton.picnic.TableSectionDsl
import com.jakewharton.picnic.TextAlignment.BottomCenter
import com.jakewharton.picnic.TextAlignment.BottomLeft
import com.jakewharton.picnic.TextAlignment.MiddleCenter
import com.jakewharton.picnic.TextAlignment.MiddleRight
import com.jakewharton.picnic.renderText
import me.saket.bytesize.ByteSize
import me.saket.bytesize.binaryBytes

internal class ArchiveFilesDiff(
  val oldFiles: ArchiveFiles,
  val newFiles: ArchiveFiles,
  val includeCompressed: Boolean = true,
) {
  data class Change(
    val path: String,
    val size: ByteSize,
    val sizeDiff: ByteSize,
    val uncompressedSize: ByteSize,
    val uncompressedSizeDiff: ByteSize,
    val type: Type,
  ) {
    enum class Type {
      Added,
      Removed,
      Changed,
    }
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
          0.binaryBytes,
          -oldFile.size,
          0.binaryBytes,
          -oldFile.uncompressedSize,
          Change.Type.Removed,
        )
      } else {
        null
      }
    }
    val changed = oldFiles.mapNotNull { (path, oldFile) ->
      val newFile =
        newFiles[path]?.let {
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
) =
  diffuseTable {
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
            cell(name) { alignment = BottomLeft }
            cell("old")
            cell("new")
            cell("diff")
          }
        }
      }

      fun TableSectionDsl.addApkRow(name: String, type: Type? = null) {
        val old = if (type != null) oldFiles.filterValues { it.type == type } else oldFiles
        val new = if (type != null) newFiles.filterValues { it.type == type } else newFiles
        val oldSize = old.values.fold(0.binaryBytes) { acc, file -> acc + file.size }
        val newSize = new.values.fold(0.binaryBytes) { acc, file -> acc + file.size }
        val oldUncompressedSize =
          old.values.fold(0.binaryBytes) { acc, file -> acc + file.uncompressedSize }
        val newUncompressedSize =
          new.values.fold(0.binaryBytes) { acc, file -> acc + file.uncompressedSize }
        if (oldSize != 0.binaryBytes || newSize != 0.binaryBytes || type !in skipIfEmptyTypes) {
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
        cellStyle { alignment = MiddleRight }
        for (type in displayTypes) {
          addApkRow(type.displayName, type)
        }
      }

      footer {
        cellStyle { alignment = MiddleRight }
        addApkRow("total")
      }
    }
    .renderText()

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
              cell("path") { alignment = BottomLeft }
            }
          }
        }
        footer {
          row {
            if (includeCompressed) {
              val totalSize = changes.fold(0.binaryBytes) { acc, change -> acc + change.size }
              val totalDiff = changes.fold(0.binaryBytes) { acc, change -> acc + change.sizeDiff }
              cell(totalSize) { alignment = MiddleRight }
              cell(totalDiff.toDiffString()) { alignment = MiddleRight }
            }
            val totalUncompressedSize =
              changes.fold(0.binaryBytes) { acc, change -> acc + change.uncompressedSize }
            val totalUncompressedDiff =
              changes.fold(0.binaryBytes) { acc, change -> acc + change.uncompressedSizeDiff }
            cell(totalUncompressedSize) { alignment = MiddleRight }
            cell(totalUncompressedDiff.toDiffString()) { alignment = MiddleRight }
            cell("(total)")
          }
        }
        for ((path, size, sizeDiff, uncompressedSize, uncompressedSizeDiff, type) in changes) {
          val typeChar =
            when (type) {
              Change.Type.Added -> '+'
              Change.Type.Removed -> '-'
              Change.Type.Changed -> '∆'
            }
          row {
            if (includeCompressed) {
              cell(if (type != Change.Type.Removed) size else "") { alignment = MiddleRight }
              cell(sizeDiff.toDiffString()) { alignment = MiddleRight }
            }
            cell(if (type != Change.Type.Removed) uncompressedSize else "") {
              alignment = MiddleRight
            }
            cell(uncompressedSizeDiff.toDiffString()) { alignment = MiddleRight }
            cell("$typeChar $path")
          }
        }
      }
      .renderText()
  )
}

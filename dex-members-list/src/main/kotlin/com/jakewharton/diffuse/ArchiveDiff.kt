package com.jakewharton.diffuse

import com.jakewharton.diffuse.ArchiveFile.Type
import com.jakewharton.picnic.SectionDsl
import com.jakewharton.picnic.TextAlignment.BottomCenter
import com.jakewharton.picnic.TextAlignment.BottomLeft
import com.jakewharton.picnic.TextAlignment.MiddleRight
import com.jakewharton.picnic.renderText

class ArchiveDiff(
  val oldFiles: List<ArchiveFile>,
  val newFiles: List<ArchiveFile>
) {
  fun toTextReport(): String = diffuseTable {
    header {
      row {
        cell("APK") {
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

  private fun SectionDsl.addApkRow(name: String, type: Type? = null) {
    val old = if (type != null) oldFiles.filter { it.type == type } else oldFiles
    val new = if (type != null) newFiles.filter { it.type == type } else newFiles
    val oldSize = old.fold(Size.ZERO) { acc, file -> acc + file.size }
    val newSize = new.fold(Size.ZERO) { acc, file -> acc + file.size }
    val oldUncompressedSize = old.fold(Size.ZERO) { acc, file -> acc + file.uncompressedSize }
    val newUncompressedSize = new.fold(Size.ZERO) { acc, file -> acc + file.uncompressedSize }
    row(name, oldSize, newSize, (newSize - oldSize).toDiffString(), oldUncompressedSize,
        newUncompressedSize, (newUncompressedSize - oldUncompressedSize).toDiffString())
  }
}

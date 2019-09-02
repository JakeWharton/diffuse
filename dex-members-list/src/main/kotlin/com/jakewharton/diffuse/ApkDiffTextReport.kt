package com.jakewharton.diffuse

import com.jakewharton.dex.ApiMapping
import com.jakewharton.diffuse.ArchiveFile.Type
import com.jakewharton.diffuse.DexDiff.ComponentDiff
import com.jakewharton.picnic.SectionDsl
import com.jakewharton.picnic.TextAlignment.BottomCenter
import com.jakewharton.picnic.TextAlignment.BottomLeft
import com.jakewharton.picnic.TextAlignment.MiddleLeft
import com.jakewharton.picnic.TextAlignment.MiddleRight
import com.jakewharton.picnic.renderText

internal class ApkDiffTextReport(private val apkDiff: ApkDiff) : DiffReport {
  override fun write(appendable: Appendable) {
    appendable.apply {
      appendln(diffuseTable {
        header {
          row("OLD/NEW", apkDiff.oldApk.filename, apkDiff.newApk.filename)
        }
        body {
          row("md5", apkDiff.oldApk.bytes.md5().hex(), apkDiff.newApk.bytes.md5().hex())
          row("sha1", apkDiff.oldApk.bytes.sha1().hex(), apkDiff.newApk.bytes.sha1().hex())
          row("mapping", apkDiff.oldMapping.toSummaryString(), apkDiff.newMapping.toSummaryString())
        }
      }.toString())
      appendln()
      appendln(apkDiff.archive.toTextReport())
      appendln()
      appendln(apkDiff.dex.toTextReport())
      appendln()
      appendln(apkDiff.arsc.toTextReport())
    }
  }

  override fun toString() = buildString { write(this) }
}

private fun ArchiveDiff.toTextReport(): String = diffuseTable {
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

  fun SectionDsl.addApkRow(name: String, type: Type? = null) {
    val old = if (type != null) oldFiles.filter { it.type == type } else oldFiles
    val new = if (type != null) newFiles.filter { it.type == type } else newFiles
    val oldSize = old.fold(Size.ZERO) { acc, file -> acc + file.size }
    val newSize = new.fold(Size.ZERO) { acc, file -> acc + file.size }
    val oldUncompressedSize = old.fold(Size.ZERO) { acc, file -> acc + file.uncompressedSize }
    val newUncompressedSize = new.fold(Size.ZERO) { acc, file -> acc + file.uncompressedSize }
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

private fun DexDiff.toTextReport(): String = diffuseTable {
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

      val addedSize = diff.added.size.toDiffString()
      val removedSize = (-diff.removed.size).toDiffString()
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

private fun ArscDiff.toTextReport() = diffuseTable {
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
      cell(oldArsc.entries.size)
      cell(newArsc.entries.size)

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

private fun ApiMapping.toSummaryString(): String {
  if (this === ApiMapping.EMPTY) {
    return "not provided"
  }
  return buildString {
    append(types.toUnitString("types", 1 to "type"))
    if (types > 0) {
      append(", ")
      append(fields.toUnitString("fields", 1 to "field"))
      append(", ")
      append(methods.toUnitString("methods", 1 to "method"))
    }
  }
}

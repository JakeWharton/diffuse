package com.jakewharton.diffuse

import com.jakewharton.dex.ApiMapping
import com.jakewharton.diffuse.ArchiveDiff.Change
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
      appendln(apkDiff.archive.toSummaryTable())
      appendln()
      appendln(apkDiff.dex.toSummaryTable())
      appendln()
      appendln(apkDiff.arsc.toSummaryTable())
      if (apkDiff.archive.changed) {
        appendln()
        appendln(apkDiff.archive.toDetailReport())
      }
      if (apkDiff.dex.changed) {
        appendln()
        appendln(apkDiff.dex.toDetailReport())
      }
      if (apkDiff.arsc.changed) {
        appendln()
        appendln(apkDiff.arsc.toDetailReport())
      }
    }
  }

  override fun toString() = buildString { write(this) }
}

private fun ArchiveDiff.toSummaryTable() = diffuseTable {
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

private fun ArchiveDiff.toDetailReport() = buildString {
  appendln("=================")
  appendln("====   APK   ====")
  appendln("=================")
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

private fun DexDiff.toSummaryTable() = diffuseTable {
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
      cell((newDexes.size - oldDexes.size).toDiffString()) {
        if (!isMultidex) {
          borderRight = false
        }
      }
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

      val addedSize = diff.added.size.toDiffString(zeroSign = '+')
      val removedSize = (-diff.removed.size).toDiffString(zeroSign = '-')
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

private fun DexDiff.toDetailReport() = buildString {
  fun appendComponentDiff(name: String, diff: ComponentDiff<*>) {
    if (diff.changed) {
      appendln()
      appendln("$name:")
      appendln()
      appendln(buildString {
        appendln(diffuseTable {
          header {
            row {
              cell("old")
              cell("new")
              cell("diff")
            }
          }

          val diffSize = (diff.added.size - diff.removed.size).toDiffString()
          val addedSize = diff.added.size.toDiffString(zeroSign = '+')
          val removedSize = (-diff.removed.size).toDiffString(zeroSign = '-')
          row(diff.oldCount, diff.newCount, "$diffSize ($addedSize $removedSize)")
        }.renderText())
        diff.added.forEach {
          appendln("+ $it")
        }
        if (diff.added.isNotEmpty() && diff.removed.isNotEmpty()) {
          appendln()
        }
        diff.removed.forEach {
          appendln("- $it")
        }
      }.prependIndent("  "))
    }
  }

  appendln("=================")
  appendln("====   DEX   ====")
  appendln("=================")
  appendComponentDiff("STRINGS", strings)
  appendComponentDiff("TYPES", types)
  appendComponentDiff("METHODS", methods)
  appendComponentDiff("FIELDS", fields)
}

private fun ArscDiff.toSummaryTable() = diffuseTable {
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

      val delta = if (entriesDelta > 0) {
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

private fun ArscDiff.toDetailReport() = buildString {
  fun <T> appendComponentDiff(name: String, added: List<T>, removed: List<T>) {
    if (added.isNotEmpty() || removed.isNotEmpty()) {
      appendln()
      appendln("$name:")
      appendln()
      appendln(buildString {
        appendln(diffuseTable {
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
          row(added.size, removed.size, "$diffSize ($addedSize $removedSize)")
        }.renderText())
        added.forEach {
          appendln("+ $it")
        }
        if (added.isNotEmpty() && removed.isNotEmpty()) {
          appendln()
        }
        removed.forEach {
          appendln("- $it")
        }
      }.prependIndent("  "))
    }
  }

  appendln("==================")
  appendln("====   ARSC   ====")
  appendln("==================")
  appendComponentDiff("CONFIGS", configsAdded, configsRemoved)
  appendComponentDiff("ENTRIES", entriesAdded, entriesRemoved)
}

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

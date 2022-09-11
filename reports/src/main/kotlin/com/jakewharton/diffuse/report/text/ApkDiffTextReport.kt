package com.jakewharton.diffuse.report.text

import com.jakewharton.diffuse.diff.ApkDiff
import com.jakewharton.diffuse.diff.lint.Notice
import com.jakewharton.diffuse.diff.toDetailReport
import com.jakewharton.diffuse.diff.toSummaryTable
import com.jakewharton.diffuse.diffuseTable
import com.jakewharton.diffuse.format.ArchiveFile.Type
import com.jakewharton.diffuse.report.Report
import com.jakewharton.diffuse.report.toSummaryString

internal class ApkDiffTextReport(private val apkDiff: ApkDiff) : Report {
  override fun write(appendable: Appendable) {
    appendable.apply {
      append("OLD: ")
      append(apkDiff.oldApk.filename)
      append(" (signature: ")
      append(apkDiff.oldApk.signatures.toSummaryString())
      appendLine(')')

      append("NEW: ")
      append(apkDiff.newApk.filename)
      append(" (signature: ")
      append(apkDiff.newApk.signatures.toSummaryString())
      appendLine(')')

      appendLine()
      if (apkDiff.lintMessages.isNotEmpty()) {
        appendLine(
          diffuseTable {
            header {
              row("NOTICES")
            }
            body {
              apkDiff.lintMessages.sorted().forEach { notice ->
                row(
                  buildString {
                    append(
                      when (notice.type) {
                        Notice.Type.Informational -> 'i'
                        Notice.Type.Warning -> '!'
                        Notice.Type.Resolution -> '✓'
                      },
                    )
                    append("  ")
                    append(notice.message)
                  },
                )
              }
            }
          }.toString(),
        )
        appendLine()
      }
      appendLine(
        apkDiff.archive.toSummaryTable(
          "APK",
          Type.APK_TYPES,
          skipIfEmptyTypes = setOf(Type.Native),
        ),
      )
      appendLine()
      appendLine(apkDiff.dex.toSummaryTable())
      appendLine()
      appendLine(apkDiff.arsc.toSummaryTable())
      if (apkDiff.archive.changed || apkDiff.signatures.changed) {
        appendLine()
        appendLine("=================")
        appendLine("====   APK   ====")
        appendLine("=================")
        if (apkDiff.archive.changed) {
          appendLine(apkDiff.archive.toDetailReport())
        }
        if (apkDiff.signatures.changed) {
          appendLine(apkDiff.signatures.toDetailReport())
        }
      }
      if (apkDiff.manifest.changed) {
        appendLine()
        appendLine("======================")
        appendLine("====   MANIFEST   ====")
        appendLine("======================")
        appendLine(apkDiff.manifest.toDetailReport())
      }
      if (apkDiff.dex.changed) {
        appendLine()
        appendLine("=================")
        appendLine("====   DEX   ====")
        appendLine("=================")
        appendLine(apkDiff.dex.toDetailReport())
      }
      if (apkDiff.arsc.changed) {
        appendLine()
        appendLine("==================")
        appendLine("====   ARSC   ====")
        appendLine("==================")
        appendLine(apkDiff.arsc.toDetailReport())
      }
    }
  }

  override fun toString() = buildString { write(this) }
}

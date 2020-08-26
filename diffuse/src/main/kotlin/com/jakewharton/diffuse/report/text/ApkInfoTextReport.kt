package com.jakewharton.diffuse.report.text

import com.jakewharton.diffuse.Apk
import com.jakewharton.diffuse.ArchiveFile
import com.jakewharton.diffuse.info.toSummaryTable
import com.jakewharton.diffuse.report.Report
import com.jakewharton.diffuse.report.toSummaryString

class ApkInfoTextReport(
  private val apk: Apk,
) : Report {
  override fun write(appendable: Appendable) {
    appendable.apply {
      append(apk.filename)
      append(" (signature: ")
      append(apk.signatures.toSummaryString())
      appendLine(')')
      appendLine()

      appendLine(
        apk.files.toSummaryTable(
          "APK",
          ArchiveFile.Type.APK_TYPES,
          skipIfEmptyTypes = setOf(ArchiveFile.Type.Native)
        )
      )
      appendLine()
//      appendLine(apkDiff.dex.toSummaryTable())
//      appendLine()
//      appendLine(apkDiff.arsc.toSummaryTable())
//      if (apkDiff.archive.changed || apkDiff.signatures.changed) {
//        appendLine()
//        appendLine("=================")
//        appendLine("====   APK   ====")
//        appendLine("=================")
//        if (apkDiff.archive.changed) {
//          appendLine(apkDiff.archive.toDetailReport())
//        }
//        if (apkDiff.signatures.changed) {
//          appendLine(apkDiff.signatures.toDetailReport())
//        }
//      }
//      if (apkDiff.manifest.changed) {
//        appendLine()
//        appendLine("======================")
//        appendLine("====   MANIFEST   ====")
//        appendLine("======================")
//        appendLine(apkDiff.manifest.toDetailReport())
//      }
//      if (apkDiff.dex.changed) {
//        appendLine()
//        appendLine("=================")
//        appendLine("====   DEX   ====")
//        appendLine("=================")
//        appendLine(apkDiff.dex.toDetailReport())
//      }
//      if (apkDiff.arsc.changed) {
//        appendLine()
//        appendLine("==================")
//        appendLine("====   ARSC   ====")
//        appendLine("==================")
//        appendLine(apkDiff.arsc.toDetailReport())
//      }
    }
  }

  override fun toString() = buildString { write(this) }
}

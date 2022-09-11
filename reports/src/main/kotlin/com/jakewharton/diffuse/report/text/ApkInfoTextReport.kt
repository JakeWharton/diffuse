package com.jakewharton.diffuse.report.text

import com.jakewharton.diffuse.format.Apk
import com.jakewharton.diffuse.format.ArchiveFile
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
          skipIfEmptyTypes = setOf(ArchiveFile.Type.Native),
        ),
      )
      appendLine()
      appendLine(apk.dexes.toSummaryTable())
      appendLine()
      appendLine(apk.arsc.toSummaryTable())
    }
  }

  override fun toString() = buildString { write(this) }
}

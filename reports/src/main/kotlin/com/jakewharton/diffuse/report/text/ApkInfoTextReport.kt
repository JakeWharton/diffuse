package com.jakewharton.diffuse.report.text

import com.jakewharton.diffuse.format.Apk
import com.jakewharton.diffuse.format.ArchiveFile
import com.jakewharton.diffuse.info.toSummaryTable
import com.jakewharton.diffuse.io.ByteUnit
import com.jakewharton.diffuse.report.Report
import com.jakewharton.diffuse.report.toSummaryString

class ApkInfoTextReport(private val apk: Apk, private val byteUnit: ByteUnit = ByteUnit.Binary) :
  Report {
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
          byteUnit = byteUnit,
        )
      )
      appendLine()
      appendLine(apk.dexes.toSummaryTable())
      appendLine()
      appendLine(apk.arsc.toSummaryTable())
    }
  }

  override fun toString() = buildString { write(this) }
}

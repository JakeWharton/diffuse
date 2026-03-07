package com.jakewharton.diffuse.info

import com.jakewharton.diffuse.format.Aar
import com.jakewharton.diffuse.report.Report
import com.jakewharton.diffuse.report.text.AarInfoTextReport

class AarInfo(private val aar: Aar) : BinaryInfo {
  override fun toTextReport(summaryOnly: Boolean): Report {
    return AarInfoTextReport(aar)
  }
}

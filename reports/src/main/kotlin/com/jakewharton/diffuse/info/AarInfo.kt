package com.jakewharton.diffuse.info

import com.jakewharton.diffuse.format.Aar
import com.jakewharton.diffuse.report.Report
import com.jakewharton.diffuse.report.html.AarInfoHtmlReport
import com.jakewharton.diffuse.report.text.AarInfoTextReport

class AarInfo(
  private val aar: Aar,
) : BinaryInfo {
  override fun toTextReport(): Report {
    return AarInfoTextReport(aar)
  }

  override fun toHtmlReport(): Report {
    return AarInfoHtmlReport(aar)
  }
}

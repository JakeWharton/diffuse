package com.jakewharton.diffuse.info

import com.jakewharton.diffuse.diff.BinaryDiff
import com.jakewharton.diffuse.format.Aab
import com.jakewharton.diffuse.report.Report
import com.jakewharton.diffuse.report.html.AabInfoHtmlReport
import com.jakewharton.diffuse.report.text.AabInfoTextReport

class AabInfo(
  private val aab: Aab,
) : BinaryDiff {
  override fun toTextReport(): Report {
    return AabInfoTextReport(aab)
  }

  override fun toHtmlReport(): Report {
    return AabInfoHtmlReport(aab)
  }
}

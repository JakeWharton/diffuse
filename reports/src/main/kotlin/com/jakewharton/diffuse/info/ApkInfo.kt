package com.jakewharton.diffuse.info

import com.jakewharton.diffuse.format.Apk
import com.jakewharton.diffuse.report.Report
import com.jakewharton.diffuse.report.html.ApkInfoHtmlReport
import com.jakewharton.diffuse.report.text.ApkInfoTextReport

class ApkInfo(
  private val apk: Apk,
) : BinaryInfo {
  override fun toTextReport(): Report {
    return ApkInfoTextReport(apk)
  }

  override fun toHtmlReport(): Report {
    return ApkInfoHtmlReport(apk)
  }
}

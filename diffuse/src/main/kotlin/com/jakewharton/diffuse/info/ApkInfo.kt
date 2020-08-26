package com.jakewharton.diffuse.info

import com.jakewharton.diffuse.ApiMapping
import com.jakewharton.diffuse.Apk
import com.jakewharton.diffuse.report.Report
import com.jakewharton.diffuse.report.text.ApkInfoTextReport

internal class ApkInfo(
  private val apk: Apk,
  private val mapping: ApiMapping,
) : BinaryInfo {
  override fun toTextReport(): Report {
    return ApkInfoTextReport(apk)
  }
}

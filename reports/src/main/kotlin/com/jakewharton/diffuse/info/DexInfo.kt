package com.jakewharton.diffuse.info

import com.jakewharton.diffuse.format.Dex
import com.jakewharton.diffuse.report.Report
import com.jakewharton.diffuse.report.text.DexInfoTextReport

class DexInfo(
  private val dex: Dex,
) : BinaryInfo {
  override fun toTextReport(): Report {
    return DexInfoTextReport(dex)
  }
}

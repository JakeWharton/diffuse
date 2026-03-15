package com.jakewharton.diffuse.info

import com.jakewharton.diffuse.format.Aar
import com.jakewharton.diffuse.io.ByteUnit
import com.jakewharton.diffuse.report.Report
import com.jakewharton.diffuse.report.text.AarInfoTextReport

class AarInfo(private val aar: Aar) : BinaryInfo {
  override fun toTextReport(summaryOnly: Boolean, byteUnit: ByteUnit): Report {
    return AarInfoTextReport(aar, byteUnit)
  }
}

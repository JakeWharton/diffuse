package com.jakewharton.diffuse.report

import com.jakewharton.diffuse.io.ByteUnit

interface Report {
  fun write(appendable: Appendable)

  interface Factory {
    fun toTextReport(summaryOnly: Boolean = false, byteUnit: ByteUnit = ByteUnit.Binary): Report

    fun toHtmlReport(summaryOnly: Boolean = false, byteUnit: ByteUnit = ByteUnit.Binary): Report {
      TODO("Implement HTML reporting")
    }
  }
}

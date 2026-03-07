package com.jakewharton.diffuse.report

interface Report {
  fun write(appendable: Appendable)

  interface Factory {
    fun toTextReport(summaryOnly: Boolean): Report

    fun toHtmlReport(summaryOnly: Boolean): Report {
      TODO("Implement HTML reporting")
    }
  }
}

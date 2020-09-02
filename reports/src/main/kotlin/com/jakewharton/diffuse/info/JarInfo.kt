package com.jakewharton.diffuse.info

import com.jakewharton.diffuse.format.Jar
import com.jakewharton.diffuse.report.Report
import com.jakewharton.diffuse.report.text.JarInfoTextReport

class JarInfo(
  private val jar: Jar,
) : BinaryInfo {
  override fun toTextReport(): Report {
    return JarInfoTextReport(jar)
  }
}

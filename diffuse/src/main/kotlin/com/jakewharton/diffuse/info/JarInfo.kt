package com.jakewharton.diffuse.info

import com.jakewharton.diffuse.Jar
import com.jakewharton.diffuse.report.Report
import com.jakewharton.diffuse.report.text.JarInfoTextReport

internal class JarInfo(
  private val jar: Jar,
) : BinaryInfo {
  override fun toTextReport(): Report {
    return JarInfoTextReport(jar)
  }
}

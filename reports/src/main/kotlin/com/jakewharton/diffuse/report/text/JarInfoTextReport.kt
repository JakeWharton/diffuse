package com.jakewharton.diffuse.report.text

import com.jakewharton.diffuse.format.ArchiveFile.Type
import com.jakewharton.diffuse.format.Jar
import com.jakewharton.diffuse.info.toSummaryTable
import com.jakewharton.diffuse.report.Report

internal class JarInfoTextReport(private val jar: Jar) : Report {
  override fun write(appendable: Appendable) {
    appendable.apply {
      appendLine(jar.filename)
      appendLine()
      appendLine(jar.files.toSummaryTable("JAR", Type.JAR_TYPES))
      appendLine()
      appendLine(listOf(jar).toSummaryTable("CLASSES"))
    }
  }

  override fun toString() = buildString { write(this) }
}

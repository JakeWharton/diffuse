package com.jakewharton.diffuse.report.text

import com.jakewharton.diffuse.format.Aar
import com.jakewharton.diffuse.format.ArchiveFile.Type
import com.jakewharton.diffuse.info.toSummaryTable
import com.jakewharton.diffuse.report.Report

internal class AarInfoTextReport(private val aar: Aar) : Report {
  override fun write(appendable: Appendable) {
    appendable.apply {
      appendLine(aar.filename)
      appendLine()
      appendLine(
        aar.files.toSummaryTable(
          "AAR",
          Type.AAR_TYPES,
          skipIfEmptyTypes = setOf(Type.JarLibs, Type.ApiJar, Type.LintJar, Type.Native, Type.Res),
        ),
      )
      appendLine()
      appendLine(aar.jars.toSummaryTable("JAR"))
    }
  }

  override fun toString() = buildString { write(this) }
}

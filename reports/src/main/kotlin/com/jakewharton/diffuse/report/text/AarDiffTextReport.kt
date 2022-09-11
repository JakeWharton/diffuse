package com.jakewharton.diffuse.report.text

import com.jakewharton.diffuse.diff.AarDiff
import com.jakewharton.diffuse.diff.toDetailReport
import com.jakewharton.diffuse.diff.toSummaryTable
import com.jakewharton.diffuse.format.ArchiveFile.Type
import com.jakewharton.diffuse.report.Report

internal class AarDiffTextReport(private val aarDiff: AarDiff) : Report {
  override fun write(appendable: Appendable) {
    appendable.apply {
      append("OLD: ")
      appendLine(aarDiff.oldAar.filename)

      append("NEW: ")
      appendLine(aarDiff.newAar.filename)

      appendLine()
      appendLine(
        aarDiff.archive.toSummaryTable(
          "AAR",
          Type.AAR_TYPES,
          skipIfEmptyTypes = setOf(Type.JarLibs, Type.ApiJar, Type.LintJar, Type.Native, Type.Res),
        ),
      )
      appendLine()
      appendLine(aarDiff.jars.toSummaryTable("JAR"))
      if (aarDiff.archive.changed) {
        appendLine()
        appendLine("=================")
        appendLine("====   AAR   ====")
        appendLine("=================")
        appendLine(aarDiff.archive.toDetailReport())
      }
      if (aarDiff.manifest.changed) {
        appendLine()
        appendLine("======================")
        appendLine("====   MANIFEST   ====")
        appendLine("======================")
        appendLine(aarDiff.manifest.toDetailReport())
      }
      if (aarDiff.jars.changed) {
        appendLine()
        appendLine("=================")
        appendLine("====   JAR   ====")
        appendLine("=================")
        appendLine(aarDiff.jars.toDetailReport())
      }
    }
  }

  override fun toString() = buildString { write(this) }
}

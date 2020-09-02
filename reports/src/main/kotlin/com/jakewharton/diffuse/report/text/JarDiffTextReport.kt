package com.jakewharton.diffuse.report.text

import com.jakewharton.diffuse.diff.JarDiff
import com.jakewharton.diffuse.diff.toDetailReport
import com.jakewharton.diffuse.diff.toSummaryTable
import com.jakewharton.diffuse.format.ArchiveFile.Type
import com.jakewharton.diffuse.report.Report

internal class JarDiffTextReport(private val jarDiff: JarDiff) : Report {
  override fun write(appendable: Appendable) {
    appendable.apply {
      append("OLD: ")
      appendLine(jarDiff.oldJar.filename)

      append("NEW: ")
      appendLine(jarDiff.newJar.filename)

      appendLine()
      appendLine(jarDiff.archive.toSummaryTable("JAR", Type.JAR_TYPES))
      appendLine()
      appendLine(jarDiff.jars.toSummaryTable("CLASSES"))
      if (jarDiff.archive.changed) {
        appendLine()
        appendLine("=================")
        appendLine("====   JAR   ====")
        appendLine("=================")
        appendLine(jarDiff.archive.toDetailReport())
      }
      if (jarDiff.jars.changed) {
        appendLine()
        appendLine("=====================")
        appendLine("====   CLASSES   ====")
        appendLine("=====================")
        appendLine(jarDiff.jars.toDetailReport())
      }
    }
  }

  override fun toString() = buildString { write(this) }
}

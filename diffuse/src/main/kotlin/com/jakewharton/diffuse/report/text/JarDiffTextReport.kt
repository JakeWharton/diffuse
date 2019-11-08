package com.jakewharton.diffuse.report.text

import com.jakewharton.diffuse.ArchiveFile.Type
import com.jakewharton.diffuse.diff.JarDiff
import com.jakewharton.diffuse.diff.toDetailReport
import com.jakewharton.diffuse.diff.toSummaryTable
import com.jakewharton.diffuse.report.DiffReport

internal class JarDiffTextReport(private val jarDiff: JarDiff) : DiffReport {
  override fun write(appendable: Appendable) {
    appendable.apply {
      append("OLD: ")
      appendln(jarDiff.oldJar.filename)

      append("NEW: ")
      appendln(jarDiff.newJar.filename)

      appendln()
      appendln(jarDiff.archive.toSummaryTable("JAR", Type.JAR_TYPES))
      appendln()
      appendln(jarDiff.jars.toSummaryTable())
      if (jarDiff.archive.changed) {
        appendln()
        appendln("=================")
        appendln("====   JAR   ====")
        appendln("=================")
        appendln(jarDiff.archive.toDetailReport())
      }
      if (jarDiff.jars.changed) {
        appendln()
        appendln("=====================")
        appendln("====   CLASSES   ====")
        appendln("=====================")
        appendln(jarDiff.jars.toDetailReport())
      }
    }
  }

  override fun toString() = buildString { write(this) }
}

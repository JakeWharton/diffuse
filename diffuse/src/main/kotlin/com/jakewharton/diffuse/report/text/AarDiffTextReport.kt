package com.jakewharton.diffuse.report.text

import com.jakewharton.diffuse.ArchiveFile.Type
import com.jakewharton.diffuse.diff.AarDiff
import com.jakewharton.diffuse.diff.toDetailReport
import com.jakewharton.diffuse.diff.toSummaryTable
import com.jakewharton.diffuse.report.DiffReport

internal class AarDiffTextReport(private val aarDiff: AarDiff) : DiffReport {
  override fun write(appendable: Appendable) {
    appendable.apply {
      append("OLD: ")
      appendln(aarDiff.oldAar.filename)

      append("NEW: ")
      appendln(aarDiff.newAar.filename)

      appendln()
      appendln(aarDiff.archive.toSummaryTable("AAR", Type.AAR_TYPES,
          skipIfEmptyTypes = setOf(Type.JarLibs, Type.ApiJar, Type.LintJar, Type.Native, Type.Res)))
      appendln()
      appendln(aarDiff.jars.toSummaryTable("JAR"))
      if (aarDiff.archive.changed) {
        appendln()
        appendln("=================")
        appendln("====   AAR   ====")
        appendln("=================")
        appendln(aarDiff.archive.toDetailReport())
      }
      if (aarDiff.manifest.changed) {
        appendln()
        appendln("======================")
        appendln("====   MANIFEST   ====")
        appendln("======================")
        appendln(aarDiff.manifest.toDetailReport())
      }
      if (aarDiff.jars.changed) {
        appendln()
        appendln("=================")
        appendln("====   JAR   ====")
        appendln("=================")
        appendln(aarDiff.jars.toDetailReport())
      }
    }
  }

  override fun toString() = buildString { write(this) }
}

package com.jakewharton.diffuse.report.html

import com.jakewharton.diffuse.diff.AarDiff
import com.jakewharton.diffuse.diff.toDetailReport
import com.jakewharton.diffuse.diff.toHtmlSummary
import com.jakewharton.diffuse.diff.toSummaryTable
import com.jakewharton.diffuse.format.ArchiveFile.Type
import com.jakewharton.diffuse.report.Report
import kotlinx.html.body
import kotlinx.html.br
import kotlinx.html.details
import kotlinx.html.h2
import kotlinx.html.head
import kotlinx.html.html
import kotlinx.html.span
import kotlinx.html.stream.appendHTML
import kotlinx.html.summary

internal class AarDiffHtmlReport(private val aarDiff: AarDiff) : Report {
  override fun write(appendable: Appendable) {
    appendable.appendHTML().html {
      head { applyStyles() }

      body {
        h2 { +"Summary" }

        span { +"OLD: ${aarDiff.oldAar.filename}" }
        br()
        span { +"NEW: ${aarDiff.newAar.filename}" }
        br()
        br()

        toSummaryTable(
          "AAR",
          aarDiff.archive,
          Type.AAR_TYPES,
          skipIfEmptyTypes = setOf(Type.JarLibs, Type.ApiJar, Type.LintJar, Type.Native, Type.Res),
        )

        br()

        toHtmlSummary("", aarDiff.jars)

        if (aarDiff.archive.changed) {
          br()
          details {
            summary { +"JAR" }
            toDetailReport(aarDiff.archive)
          }
        }

        if (aarDiff.jars.changed) {
          br()
          details {
            summary { +"CLASSFILES" }
            toDetailReport(aarDiff.jars)
          }
        }
      }
    }
  }

  override fun toString() = buildString { write(this) }
}

package com.jakewharton.diffuse.report.html

import com.jakewharton.diffuse.diff.JarDiff
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
import kotlinx.html.style
import kotlinx.html.summary
import kotlinx.html.unsafe

internal class JarDiffHtmlReport(private val jarDiff: JarDiff) : Report {
  override fun write(appendable: Appendable) {
    appendable.appendHTML().html {
      head {
        style(type = "text/css") {
          unsafe {
            raw(
              """
              table{
                border-collapse:collapse;
                border:1px solid #000;
              }
  
              table td{
                border:1px solid #000;
              }
              """.trimIndent(),
            )
          }
        }
      }

      body {
        h2 { +"Summary" }

        span { +"OLD: ${jarDiff.oldJar.filename}" }
        br()
        span { +"NEW: ${jarDiff.newJar.filename}" }
        br()
        br()

        toSummaryTable("JAR", jarDiff.archive, Type.JAR_TYPES)

        br()

        toHtmlSummary("", jarDiff.jars)

        if (jarDiff.archive.changed) {
          br()
          details {
            summary { +"JAR" }
            toDetailReport(jarDiff.archive)
          }
        }

        if (jarDiff.jars.changed) {
          br()
          details {
            summary { +"CLASSFILES" }
            toDetailReport(jarDiff.jars)
          }
        }
      }
    }
  }

  override fun toString() = buildString { write(this) }
}

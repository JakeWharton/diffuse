package com.jakewharton.diffuse.report.html

import com.jakewharton.diffuse.diff.ApkDiff
import com.jakewharton.diffuse.diff.toDetailReport
import com.jakewharton.diffuse.diff.toSummaryTable
import com.jakewharton.diffuse.format.ArchiveFile.Type
import com.jakewharton.diffuse.report.Report
import com.jakewharton.diffuse.report.toSummaryString
import kotlinx.html.body
import kotlinx.html.br
import kotlinx.html.details
import kotlinx.html.head
import kotlinx.html.html
import kotlinx.html.span
import kotlinx.html.stream.appendHTML
import kotlinx.html.style
import kotlinx.html.summary
import kotlinx.html.unsafe

internal class ApkDiffHtmlReport(
  private val diff: ApkDiff,
) : Report {
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
        span { +"OLD: ${diff.oldApk.filename} (signature: ${diff.oldApk.signatures.toSummaryString()})" }
        span { +"NEW: ${diff.newApk.filename} (signature: ${diff.newApk.signatures.toSummaryString()})" }
        br()
        br()

        toSummaryTable("APK", diff.archive, Type.APK_TYPES, skipIfEmptyTypes = setOf(Type.Native))
        br()

        toSummaryTable(diff.dex)
        br()

        toSummaryTable(diff.arsc)
        br()

        if (diff.archive.changed) {
          details {
            summary { +"Archive" }
            toDetailReport(diff.archive)
          }
        }

        if (diff.signatures.changed) {
          details {
            summary { +"Signatures" }
            toDetailReport(diff.signatures)
          }
        }

        if (diff.manifest.changed) {
          details {
            summary { +"Manifest" }
            toDetailReport(diff.manifest)
          }
        }

        if (diff.dex.changed) {
          details {
            summary { +"Dex" }
            toDetailReport(diff.dex)
          }
        }

        if (diff.arsc.changed) {
          details {
            summary { +"ARSC" }
            toDetailReport(diff.arsc)
          }
        }
      }
    }
  }

  override fun toString() = buildString { write(this) }
}

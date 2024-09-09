package com.jakewharton.diffuse.report.html

import com.jakewharton.diffuse.format.Aar
import com.jakewharton.diffuse.format.ArchiveFile.Type
import com.jakewharton.diffuse.info.toSummaryTable
import com.jakewharton.diffuse.report.Report
import kotlinx.html.body
import kotlinx.html.h2
import kotlinx.html.head
import kotlinx.html.html
import kotlinx.html.stream.appendHTML
import kotlinx.html.style
import kotlinx.html.unsafe

class AarInfoHtmlReport(
  private val aar: Aar,
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
        appendable.apply {
          h2 { +aar.filename.toString() }

          toSummaryTable(
            "AAR",
            aar.files,
            Type.AAR_TYPES,
            skipIfEmptyTypes = setOf(Type.JarLibs, Type.ApiJar, Type.LintJar, Type.Native, Type.Res),
          )

          toSummaryTable("JAR", aar.jars)
        }
      }
    }
  }

  override fun toString() = buildString { write(this) }
}

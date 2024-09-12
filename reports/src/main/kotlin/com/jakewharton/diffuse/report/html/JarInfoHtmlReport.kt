package com.jakewharton.diffuse.report.html

import com.jakewharton.diffuse.format.ArchiveFile.Type
import com.jakewharton.diffuse.format.Jar
import com.jakewharton.diffuse.info.toSummaryTable
import com.jakewharton.diffuse.report.Report
import kotlinx.html.body
import kotlinx.html.br
import kotlinx.html.details
import kotlinx.html.head
import kotlinx.html.html
import kotlinx.html.p
import kotlinx.html.stream.appendHTML
import kotlinx.html.summary

internal class JarInfoHtmlReport(private val jar: Jar) : Report {
  override fun write(appendable: Appendable) {
    appendable.appendHTML().html {
      head { applyStyles() }

      body {
        p { +jar.filename!! }

        toSummaryTable("JAR", jar.files, Type.JAR_TYPES)

        br()

        details {
          summary { +"CLASSFILE INFO" }

          toSummaryTable("entity", listOf(jar))
        }
      }
    }
  }

  override fun toString() = buildString { write(this) }
}

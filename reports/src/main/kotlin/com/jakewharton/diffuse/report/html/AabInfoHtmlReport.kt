package com.jakewharton.diffuse.report.html

import com.jakewharton.diffuse.format.Aab
import com.jakewharton.diffuse.format.ArchiveFile
import com.jakewharton.diffuse.info.toSummaryTable
import com.jakewharton.diffuse.report.Report
import kotlinx.html.BODY
import kotlinx.html.body
import kotlinx.html.details
import kotlinx.html.head
import kotlinx.html.html
import kotlinx.html.p
import kotlinx.html.stream.appendHTML
import kotlinx.html.style
import kotlinx.html.summary
import kotlinx.html.table
import kotlinx.html.tbody
import kotlinx.html.td
import kotlinx.html.thead
import kotlinx.html.tr
import kotlinx.html.unsafe

class AabInfoHtmlReport(private val aab: Aab) : Report {
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
        p { +aab.filename.toString() }

        table {
          thead {
            tr {
              td { +"MODULES" }
            }
          }

          tbody {
            tr {
              td { +"base" }
            }

            for (name in aab.featureModules.keys) {
              tr { td { +name } }
            }
          }
        }

        appendModule("base", aab.baseModule)

        for ((name, module) in aab.featureModules) {
          appendModule(name, module)
        }
      }
    }
  }

  override fun toString() = buildString { write(this) }

  private fun BODY.appendModule(
    name: String,
    module: Aab.Module,
  ) {
    details {
      summary { +name }
      toSummaryTable(
        "AAB",
        module.files,
        ArchiveFile.Type.AAB_TYPES,
        skipIfEmptyTypes = setOf(ArchiveFile.Type.Native),
      )

      toSummaryTable(module.dexes)
    }
  }
}

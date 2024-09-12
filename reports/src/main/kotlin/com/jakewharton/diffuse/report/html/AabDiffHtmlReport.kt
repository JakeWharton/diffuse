package com.jakewharton.diffuse.report.html

import com.jakewharton.diffuse.diff.AabDiff
import com.jakewharton.diffuse.diff.toDetailReport
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
import kotlinx.html.table
import kotlinx.html.td
import kotlinx.html.thead
import kotlinx.html.tr

internal class AabDiffHtmlReport(private val aabDiff: AabDiff) : Report {
  override fun write(appendable: Appendable) {
    appendable.appendHTML().html {
      head { applyStyles() }

      body {
        span { +"OLD: ${aabDiff.oldAab.filename}" }
        span { +"NEW: ${aabDiff.newAab.filename}" }
        br()

        table {
          style = "text-align: center; vertical-align: middle;"

          thead {
            style = "text-align: left; vertical-align: bottom;"
            tr {
              td { +"MODULES" }
              td { +"old" }
              td { +"new" }
            }

            tr {
              td {
                style = "text-align: right; vertical-align: middle;"
                +"base"
              }
              td { +"✓" }
              td { +"✓" }
            }

            for (name in aabDiff.featureModuleNames) {
              tr {
                td {
                  style = "text-align: right; vertical-align: middle;"
                  +name
                }
                td { if (name in aabDiff.oldAab.featureModules) +"✓" else +"" }
                td { if (name in aabDiff.newAab.featureModules) +"✓" else +"" }
              }
            }
          }
        }

        h2 { +"base" }
        if (aabDiff.baseModule.archive.changed) {
          toDetailReport(aabDiff.baseModule.archive)
        }
        if (aabDiff.baseModule.dex.changed) {
          toDetailReport(aabDiff.baseModule.dex)
        }
        if (aabDiff.baseModule.manifest.changed) {
          toDetailReport(aabDiff.baseModule.manifest)
        }

        for (name in (aabDiff.featureModuleNames - aabDiff.removedFeatureModules.keys)) {
          details {
            summary { +name }

            val addedModule = aabDiff.addedFeatureModules[name]
            val changedModule = aabDiff.changedFeatureModules[name]
            assert((addedModule != null) xor (changedModule != null))

            if (addedModule != null) {
              // TODO
            }
            if (changedModule != null) {
              if (changedModule.archive.changed) {
                toDetailReport(changedModule.archive)
              }
              if (changedModule.dex.changed) {
                toDetailReport(changedModule.dex)
              }
              if (changedModule.manifest.changed) {
                toDetailReport(changedModule.manifest)
              }
            }
          }
        }
      }
    }
  }

  override fun toString() = buildString { write(this) }
}

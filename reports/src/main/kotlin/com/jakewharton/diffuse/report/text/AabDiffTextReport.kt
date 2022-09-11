package com.jakewharton.diffuse.report.text

import com.jakewharton.diffuse.diff.AabDiff
import com.jakewharton.diffuse.diff.toDetailReport
import com.jakewharton.diffuse.diffuseTable
import com.jakewharton.diffuse.report.Report
import com.jakewharton.picnic.TextAlignment.BottomLeft
import com.jakewharton.picnic.TextAlignment.MiddleCenter
import com.jakewharton.picnic.TextAlignment.MiddleRight

internal class AabDiffTextReport(private val aabDiff: AabDiff) : Report {
  override fun write(appendable: Appendable) {
    appendable.apply {
      append("OLD: ")
      appendLine(aabDiff.oldAab.filename)

      append("NEW: ")
      appendLine(aabDiff.newAab.filename)

      appendLine()
      appendLine(
        diffuseTable {
          cellStyle {
            alignment = MiddleCenter
          }

          header {
            cellStyle {
              alignment = BottomLeft
            }
            row {
              cell("MODULES")
              cell("old")
              cell("new")
            }
          }

          row {
            cell("base") {
              alignment = MiddleRight
            }
            cell("✓")
            cell("✓")
          }

          for (name in aabDiff.featureModuleNames) {
            row {
              cell(name) {
                alignment = MiddleRight
              }
              cell(if (name in aabDiff.oldAab.featureModules) "✓" else "")
              cell(if (name in aabDiff.newAab.featureModules) "✓" else "")
            }
          }
        }.toString(),
      )

      appendLine("==================")
      appendLine("====   base   ====")
      appendLine("==================")
      appendLine()
      if (aabDiff.baseModule.archive.changed) {
        appendLine(aabDiff.baseModule.archive.toDetailReport())
      }
      if (aabDiff.baseModule.dex.changed) {
        appendLine(aabDiff.baseModule.dex.toDetailReport())
      }
      if (aabDiff.baseModule.manifest.changed) {
        appendLine(aabDiff.baseModule.manifest.toDetailReport())
      }

      for (name in (aabDiff.featureModuleNames - aabDiff.removedFeatureModules.keys)) {
        appendLine()
        appendLine()
        appendLine("==============${"=".repeat(name.length)}")
        appendLine("====   $name   ====")
        appendLine("==============${"=".repeat(name.length)}")
        appendLine()

        val addedModule = aabDiff.addedFeatureModules[name]
        val changedModule = aabDiff.changedFeatureModules[name]
        assert((addedModule != null) xor (changedModule != null))

        if (addedModule != null) {
          // TODO
        }
        if (changedModule != null) {
          if (changedModule.archive.changed) {
            appendLine(changedModule.archive.toDetailReport())
          }
          if (changedModule.dex.changed) {
            appendLine(changedModule.dex.toDetailReport())
          }
          if (changedModule.manifest.changed) {
            appendLine(changedModule.manifest.toDetailReport())
          }
        }
      }
    }
  }

  override fun toString() = buildString { write(this) }
}

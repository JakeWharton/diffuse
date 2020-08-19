package com.jakewharton.diffuse.report.text

import com.jakewharton.diffuse.diff.AabDiff
import com.jakewharton.diffuse.diff.toDetailReport
import com.jakewharton.diffuse.diffuseTable
import com.jakewharton.diffuse.report.DiffReport
import com.jakewharton.picnic.TextAlignment.BottomLeft
import com.jakewharton.picnic.TextAlignment.MiddleCenter
import com.jakewharton.picnic.TextAlignment.MiddleRight

internal class AabDiffTextReport(private val aabDiff: AabDiff) : DiffReport {
  override fun write(appendable: Appendable) {
    appendable.apply {
      append("OLD: ")
      appendln(aabDiff.oldAab.filename)

      append("NEW: ")
      appendln(aabDiff.newAab.filename)

      appendln()
      appendln(
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
        }.toString()
      )

      // TODO base module

      for (name in (aabDiff.featureModuleNames - aabDiff.removedFeatureModules.keys)) {
        appendln()
        appendln()
        appendln("==============${"=".repeat(name.length)}")
        appendln("====   $name   ====")
        appendln("==============${"=".repeat(name.length)}")
        appendln()

        val addedModule = aabDiff.addedFeatureModules[name]
        val changedModule = aabDiff.changedFeatureModules[name]
        assert((addedModule != null) xor (changedModule != null))

        if (addedModule != null) {
          // TODO
        }
        if (changedModule != null) {
          if (changedModule.archive.changed) {
            appendln(changedModule.archive.toDetailReport())
          }
          if (changedModule.dex.changed) {
            appendln(changedModule.dex.toDetailReport())
          }
          if (changedModule.manifest.changed) {
            appendln(changedModule.manifest.toDetailReport())
          }
        }
      }
    }
  }

  override fun toString() = buildString { write(this) }
}

package com.jakewharton.diffuse.report.text

import com.jakewharton.diffuse.diffuseTable
import com.jakewharton.diffuse.format.Aab
import com.jakewharton.diffuse.format.ArchiveFile
import com.jakewharton.diffuse.info.toSummaryTable
import com.jakewharton.diffuse.report.Report

internal class AabInfoTextReport(private val aab: Aab) : Report {
  override fun write(appendable: Appendable) {
    appendable.apply {
      appendLine(aab.filename)
      appendLine()
      appendLine(
        diffuseTable {
          header {
            row("MODULES")
          }

          row("base")

          for (name in aab.featureModules.keys) {
            row(name)
          }
        }.toString(),
      )

      appendModule("base", aab.baseModule)

      for ((name, module) in aab.featureModules) {
        appendModule(name, module)
      }
    }
  }

  private fun Appendable.appendModule(
    name: String,
    module: Aab.Module,
  ) {
    appendLine()
    appendLine()
    appendLine("==============${"=".repeat(name.length)}")
    appendLine("====   $name   ====")
    appendLine("==============${"=".repeat(name.length)}")
    appendLine()

    appendLine(
      module.files.toSummaryTable(
        "AAB",
        ArchiveFile.Type.AAB_TYPES,
        skipIfEmptyTypes = setOf(ArchiveFile.Type.Native),
      ),
    )
    appendLine(module.dexes.toSummaryTable())
  }

  override fun toString() = buildString { write(this) }
}

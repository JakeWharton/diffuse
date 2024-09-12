package com.jakewharton.diffuse.diff

import com.github.difflib.DiffUtils
import com.github.difflib.UnifiedDiffUtils
import com.jakewharton.diffuse.diffuseTable
import com.jakewharton.diffuse.format.AndroidManifest
import kotlinx.html.FlowContent
import kotlinx.html.br
import kotlinx.html.span
import kotlinx.html.table
import kotlinx.html.tbody
import kotlinx.html.td
import kotlinx.html.thead
import kotlinx.html.tr

internal class ManifestDiff(
  val oldManifest: AndroidManifest,
  val newManifest: AndroidManifest,
) {
  internal val parsedPropertiesChanged = oldManifest.packageName != newManifest.packageName ||
    oldManifest.versionName != newManifest.versionName ||
    oldManifest.versionCode != newManifest.versionCode

  val diff: List<String> = run {
    val oldLines = oldManifest.xml.lines()
    val newLines = newManifest.xml.lines()
    val diff = DiffUtils.diff(oldLines, newLines)
    UnifiedDiffUtils.generateUnifiedDiff(AndroidManifest.NAME, AndroidManifest.NAME, oldLines, diff, 1)
  }

  val changed = parsedPropertiesChanged || diff.isNotEmpty()
}

internal fun ManifestDiff.toDetailReport() = buildString {
  if (parsedPropertiesChanged) {
    appendLine()
    appendLine(
      diffuseTable {
        header {
          row("", "old", "new")
        }
        row("package", oldManifest.packageName, newManifest.packageName)
        row("version code", oldManifest.versionCode, newManifest.versionCode)
        row("version name", oldManifest.versionName, newManifest.versionName)
      },
    )
  }
  if (diff.isNotEmpty()) {
    appendLine()
    diff.drop(2) // Skip file name headers
      .forEach { appendLine(it) }
    appendLine()
  }
}

internal fun FlowContent.toDetailReport(diff: ManifestDiff) {
  if (diff.parsedPropertiesChanged) {
    table {
      thead {
        tr {
          td { +"" }
          td { +"old" }
          td { +"new" }
        }
      }
      tbody {
        tr {
          td { +"package" }
          td { +diff.oldManifest.packageName }
          td { +diff.newManifest.packageName }
        }

        tr {
          td { +"version code" }
          td { +diff.oldManifest.versionCode.toString() }
          td { +diff.newManifest.versionCode.toString() }
        }

        tr {
          td { +"version name" }
          td { +diff.oldManifest.versionName.toString() }
          td { +diff.newManifest.versionName.toString() }
        }
      }
    }
  }

  if (diff.diff.isNotEmpty()) {
    diff.diff.drop(2) // Skip file name headers
      .forEach {
        span { +it }
        br()
      }
  }
}

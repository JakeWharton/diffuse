package com.jakewharton.diffuse.diff

import com.github.difflib.DiffUtils
import com.github.difflib.UnifiedDiffUtils
import com.jakewharton.diffuse.diffuseTable
import com.jakewharton.diffuse.format.AndroidManifest

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

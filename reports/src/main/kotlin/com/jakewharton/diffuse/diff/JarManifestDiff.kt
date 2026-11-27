package com.jakewharton.diffuse.diff

import com.github.difflib.DiffUtils
import com.github.difflib.UnifiedDiffUtils

internal class JarManifestDiff(oldManifest: String, newManifest: String) {
  val diff: List<String> =
    if (oldManifest == newManifest) {
      emptyList()
    } else {
      val oldLines = oldManifest.lines()
      val newLines = newManifest.lines()
      val diff = DiffUtils.diff(oldLines, newLines)
      UnifiedDiffUtils.generateUnifiedDiff(MANIFEST_PATH, MANIFEST_PATH, oldLines, diff, 1)
    }

  val changed = diff.isNotEmpty()

  internal companion object {
    const val MANIFEST_PATH = "META-INF/MANIFEST.MF"
  }
}

internal fun JarManifestDiff.toDetailReport() = buildString {
  if (diff.isNotEmpty()) {
    appendLine()
    diff
      .drop(2) // Skip file name headers.
      .forEach(::appendLine)
    appendLine()
  }
}

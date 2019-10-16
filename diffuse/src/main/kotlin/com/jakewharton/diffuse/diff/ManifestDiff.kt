package com.jakewharton.diffuse.diff

import com.jakewharton.diffuse.Manifest
import com.jakewharton.diffuse.diffuseTable

internal class ManifestDiff(
  val oldManifest: Manifest,
  val newManifest: Manifest
) {
  val changed get() = oldManifest.packageName != newManifest.packageName ||
      oldManifest.versionName != newManifest.versionName ||
      oldManifest.versionCode != newManifest.versionCode
}

internal fun ManifestDiff.toDetailReport() = buildString {
  appendln()
  appendln(diffuseTable {
    header {
      row("", "old", "new")
    }
    row("package", oldManifest.packageName, newManifest.packageName)
    row("version code", oldManifest.versionCode, newManifest.versionCode)
    row("version name", oldManifest.versionName, newManifest.versionName)
  })
}

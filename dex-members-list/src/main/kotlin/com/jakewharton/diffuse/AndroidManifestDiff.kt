package com.jakewharton.diffuse

internal class AndroidManifestDiff(
  val oldManifest: AndroidManifest,
  val newManifest: AndroidManifest
) {
  val changed get() = oldManifest.packageName != newManifest.packageName ||
      oldManifest.versionName != newManifest.versionName ||
      oldManifest.versionCode != newManifest.versionCode
}

internal fun AndroidManifestDiff.toDetailReport() = buildString {
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

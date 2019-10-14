package com.jakewharton.diffuse

import com.jakewharton.diffuse.AndroidManifest.Companion.toAndroidManifest
import com.jakewharton.diffuse.ArchiveFile.Type.Companion.toApkFileType
import com.jakewharton.diffuse.ArchiveFiles.Companion.toArchiveFiles
import com.jakewharton.diffuse.Arsc.Companion.toArsc
import com.jakewharton.diffuse.Dex.Companion.toDex
import com.jakewharton.diffuse.Signatures.Companion.toSignatures
import com.jakewharton.diffuse.io.Input

class Apk private constructor(
  override val filename: String?,
  val files: ArchiveFiles,
  val dexes: List<Dex>,
  val arsc: Arsc,
  val manifest: AndroidManifest,
  val signatures: Signatures
) : Binary {
  companion object {
    internal val classesDexRegex = Regex("classes\\d*\\.dex")

    fun Input.toApk(): Apk {
      val signatures = toSignatures()
      toZip().use { zip ->
        val files = zip.toArchiveFiles { it.toApkFileType() }
        val manifest = zip["AndroidManifest.xml"].input().toBinaryResourceFile().toAndroidManifest()
        val dexes = zip.entries
            .filter { it.path.matches(classesDexRegex) }
            .map { it.input().toDex() }
        val arsc = zip["resources.arsc"].input().toBinaryResourceFile().toArsc()
        return Apk(name, files, dexes, arsc, manifest, signatures)
      }
    }
  }
}

internal fun Signatures.toSummaryString(): String {
  if (v1.isEmpty() && v2.isEmpty() && v3.isEmpty()) {
    return "none"
  }
  return buildString {
    if (v1.isNotEmpty()) {
      append("V1")
      if (v1.size > 1) {
        append(" (x")
        append(v1.size)
        append(')')
      }
    }
    if (v2.isNotEmpty()) {
      if (length > 0) {
        append(", ")
      }
      append("V2")
      if (v2.size > 1) {
        append(" (x")
        append(v2.size)
        append(')')
      }
    }
    if (v3.isNotEmpty()) {
      if (length > 0) {
        append(", ")
      }
      append("V3")
      if (v3.size > 1) {
        append(" (x")
        append(v3.size)
        append(')')
      }
    }
  }
}

package com.jakewharton.diffuse

import com.jakewharton.dex.entries
import com.jakewharton.dex.readBytes
import com.jakewharton.diffuse.AndroidManifest.Companion.toAndroidManifest
import com.jakewharton.diffuse.ArchiveFile.Type.Companion.toApkFileType
import com.jakewharton.diffuse.ArchiveFiles.Companion.toArchiveFiles
import com.jakewharton.diffuse.Arsc.Companion.toArsc
import com.jakewharton.diffuse.Dex.Companion.toDex
import com.jakewharton.diffuse.Signatures.Companion.toSignatures
import okio.ByteString.Companion.toByteString
import java.nio.file.Path

class Apk private constructor(
  override val filename: String?,
  val files: ArchiveFiles,
  val dexes: List<Dex>,
  val arsc: Arsc,
  val manifest: AndroidManifest,
  val signatures: Signatures
) : Binary {
  companion object {
    @JvmStatic
    @JvmName("create")
    fun Path.toApk(): Apk {
      val bytes = readBytes().toByteString()
      val files = bytes.toArchiveFiles { it.toApkFileType() }
      val dexes = bytes.asInputStream().asZip().use { zis ->
        zis.entries()
            .filter { it.name.endsWith(".dex") }
            .map { zis.readByteString().toDex() }
            .toList()
      }
      val arsc = bytes.asInputStream().asZip().use { zis ->
        zis.entries().first { it.name.endsWith(".arsc") }
        zis.readByteString().toArsc()
      }
      val manifest = bytes.asInputStream().asZip().use { zis ->
        zis.entries().first { it.name == "AndroidManifest.xml" }
        zis.readByteString().toAndroidManifest()
      }
      val signatures = toSignatures()
      return Apk(fileName.toString(), files, dexes, arsc, manifest, signatures)
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

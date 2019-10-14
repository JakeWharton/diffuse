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

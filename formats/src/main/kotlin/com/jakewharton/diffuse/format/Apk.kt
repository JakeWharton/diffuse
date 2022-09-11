package com.jakewharton.diffuse.format

import com.jakewharton.diffuse.format.AndroidManifest.Companion.toManifest
import com.jakewharton.diffuse.format.ArchiveFile.Type.Companion.toApkFileType
import com.jakewharton.diffuse.format.ArchiveFiles.Companion.toArchiveFiles
import com.jakewharton.diffuse.format.Arsc.Companion.toArsc
import com.jakewharton.diffuse.format.Dex.Companion.toDex
import com.jakewharton.diffuse.format.Signatures.Companion.toSignatures
import com.jakewharton.diffuse.io.Input

class Apk private constructor(
  override val filename: String?,
  val files: ArchiveFiles,
  val dexes: List<Dex>,
  val arsc: Arsc,
  val manifest: AndroidManifest,
  val signatures: Signatures,
) : BinaryFormat {
  companion object {
    internal val classesDexRegex = Regex("classes\\d*\\.dex")

    @JvmStatic
    @JvmName("parse")
    fun Input.toApk(): Apk {
      val signatures = toSignatures()
      toZip().use { zip ->
        val files = zip.toArchiveFiles { it.toApkFileType() }
        val arsc = zip[Arsc.NAME].asInput().toArsc()
        val manifest = zip[AndroidManifest.NAME].asInput().toBinaryResourceFile().toManifest(arsc)
        val dexes = zip.entries
          .filter { it.path.matches(classesDexRegex) }
          .map { it.asInput().toDex() }
        return Apk(name, files, dexes, arsc, manifest, signatures)
      }
    }
  }
}

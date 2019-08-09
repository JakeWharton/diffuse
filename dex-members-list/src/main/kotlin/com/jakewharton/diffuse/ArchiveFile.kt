package com.jakewharton.diffuse

data class ArchiveFile(
  val path: String,
  val type: Type,
  val size: Size,
  val uncompressedSize: Size
) {
  enum class Type {
    Dex, Arsc, Manifest, Res, Asset, Native, Other;

    companion object {
      private val dexRegex = Regex("classes\\d*\\.dex")

      @JvmStatic
      @JvmName("fromName")
      fun String.toApkFileType() = when {
          matches(dexRegex) -> Dex
          equals("AndroidManifest.xml") -> Manifest
          equals("resources.arsc") -> Arsc
          startsWith("lib/") -> Native
          startsWith("assets/") -> Asset
          startsWith("res/") -> Res
          else -> Other
        }
    }
  }
}

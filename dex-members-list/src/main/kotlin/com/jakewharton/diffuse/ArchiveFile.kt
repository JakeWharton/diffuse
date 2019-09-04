package com.jakewharton.diffuse

import com.jakewharton.dex.entries
import com.jakewharton.diffuse.ArchiveFile.Type
import okio.Buffer
import okio.ByteString
import java.util.Locale
import java.util.SortedMap
import java.util.zip.ZipInputStream

data class ArchiveFile(
  val path: String,
  val type: Type,
  val size: Size,
  val uncompressedSize: Size
) {
  enum class Type {
    Dex,
    Jar,
    ApiJar {
      override val displayName get() = "api-jar"
    },
    LintJar {
      override val displayName get() = "lint-jar"
    },
    JarLibs {
      override val displayName get() = "libs"
    },
    Arsc,
    Manifest,
    Res,
    Asset,
    Native,
    Other;

    open val displayName get() = name.toLowerCase(Locale.US)

    companion object {
      @JvmField
      val APK_TYPES = listOf(Dex, Arsc, Manifest, Res, Native, Asset, Other)

      @JvmField
      val AAR_TYPES = listOf(Jar, Manifest, Res, Native, JarLibs, ApiJar, LintJar, Other)

      private val dexRegex = Regex("classes\\d*\\.dex")

      @JvmStatic
      @JvmName("fromApkName")
      fun String.toApkFileType() = when {
        matches(dexRegex) -> Dex
        equals("AndroidManifest.xml") -> Manifest
        equals("resources.arsc") -> Arsc
        startsWith("lib/") -> Native
        startsWith("assets/") -> Asset
        startsWith("res/") -> Res
        else -> Other
      }

      @JvmStatic
      @JvmName("fromAarName")
      fun String.toAarFileType() = when {
        equals("classes.jar") -> Jar
        equals("api.jar") -> ApiJar
        equals("lint.jar") -> LintJar
        equals("AndroidManifest.xml") -> Manifest
        startsWith("jni/") -> Native
        startsWith("libs/") -> JarLibs
        startsWith("assets/") -> Asset
        startsWith("res/") -> Res
        else -> Other
      }
    }
  }
}

internal fun ByteString.parseArchiveFiles(
  nameToType: (String) -> Type
): SortedMap<String, ArchiveFile> {
  return ZipInputStream(Buffer().write(this).inputStream()).use { zis ->
    zis.entries()
        .associate { entry ->
          entry.name to ArchiveFile(entry.name, nameToType(entry.name), Size(entry.compressedSize),
              Size(entry.size))
        }
        .toSortedMap()
  }
}

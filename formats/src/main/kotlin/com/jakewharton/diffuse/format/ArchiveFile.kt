package com.jakewharton.diffuse.format

import com.jakewharton.diffuse.format.Arsc as ArscFormat
import dev.drewhamilton.poko.Poko
import java.util.Locale
import me.saket.bytesize.ByteSize

@Poko
class ArchiveFile(
  val path: String,
  val type: Type,
  val size: ByteSize,
  val uncompressedSize: ByteSize,
  val isCompressed: Boolean,
) {
  fun copy(
    path: String = this.path,
    type: Type = this.type,
    size: ByteSize = this.size,
    uncompressedSize: ByteSize = this.uncompressedSize,
    isCompressed: Boolean = this.isCompressed,
  ): ArchiveFile = ArchiveFile(path, type, size, uncompressedSize, isCompressed)

  enum class Type {
    Dex,
    Jar,
    ApiJar {
      override val displayName
        get() = "api-jar"
    },
    LintJar {
      override val displayName
        get() = "lint-jar"
    },
    JarLibs {
      override val displayName
        get() = "libs"
    },
    Class,
    Arsc,
    Manifest,
    Res,
    Asset,
    Native,
    Dir,
    Other;

    open val displayName
      get() = name.lowercase(Locale.US)

    companion object {
      @JvmField val APK_TYPES = listOf(Dex, Arsc, Manifest, Res, Native, Asset, Dir, Other)

      @JvmField val AAB_TYPES = listOf(Dex, Manifest, Res, Native, Asset, Dir, Other)

      @JvmField
      val AAR_TYPES = listOf(Jar, Manifest, Res, Native, JarLibs, ApiJar, LintJar, Dir, Other)

      @JvmField val JAR_TYPES = listOf(Class, Dir, Other)

      @JvmStatic
      @JvmName("fromApkName")
      fun String.toApkFileType() =
        when {
          endsWith("/") -> Dir
          matches(Apk.classesDexRegex) -> Dex
          equals(AndroidManifest.NAME) -> Manifest
          equals(ArscFormat.NAME) -> Arsc
          startsWith("lib/") -> Native
          startsWith("assets/") -> Asset
          startsWith("res/") -> Res
          else -> Other
        }

      @JvmStatic
      @JvmName("fromAabName")
      fun String.toAabFileType() =
        when {
          endsWith("/") -> Dir
          equals(Aab.Module.MANIFEST_FILE_PATH) -> Manifest
          startsWith("dex/") -> Dex
          startsWith("lib/") -> Native
          startsWith("assets/") -> Asset
          startsWith("res/") -> Res
          else -> Other
        }

      @JvmStatic
      @JvmName("fromAarName")
      fun String.toAarFileType() =
        when {
          endsWith("/") -> Dir
          equals("classes.jar") -> Jar
          equals("api.jar") -> ApiJar
          equals("lint.jar") -> LintJar
          equals(AndroidManifest.NAME) -> Manifest
          startsWith("jni/") -> Native
          matches(Aar.libsJarRegex) -> JarLibs
          startsWith("assets/") -> Asset
          startsWith("res/") -> Res
          else -> Other
        }

      @JvmStatic
      @JvmName("fromJarName")
      fun String.toJarFileType() =
        when {
          endsWith("/") -> Dir
          endsWith(".class") -> Class
          else -> Other
        }
    }
  }
}

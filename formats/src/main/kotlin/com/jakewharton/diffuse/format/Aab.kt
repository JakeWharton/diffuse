package com.jakewharton.diffuse.format

import com.android.aapt.Resources.XmlNode
import com.jakewharton.diffuse.format.Aab.Module.Companion.toModule
import com.jakewharton.diffuse.format.AndroidManifest.Companion.toManifest
import com.jakewharton.diffuse.format.ApiMapping.Companion.toApiMapping
import com.jakewharton.diffuse.format.ArchiveFile.Type.Companion.toAabFileType
import com.jakewharton.diffuse.format.ArchiveFiles.Companion.toArchiveFiles
import com.jakewharton.diffuse.format.Dex.Companion.toDex
import com.jakewharton.diffuse.io.Input
import com.jakewharton.diffuse.io.Zip

class Aab private constructor(
  override val filename: String?,
  val apiMapping: ApiMapping,
  val baseModule: Module,
  val featureModules: Map<String, Module>,
) : BinaryFormat {
  // TODO remove toTypedArray call https://youtrack.jetbrains.com/issue/KT-12663
  val modules get() = listOf(baseModule, *featureModules.values.toTypedArray())

  class Module private constructor(
    val files: ArchiveFiles,
    val manifest: AndroidManifest,
    val dexes: List<Dex>,
  ) {
    companion object {
      internal const val MANIFEST_FILE_PATH = "manifest/${AndroidManifest.NAME}"

      fun Zip.toModule(): Module {
        val files = toArchiveFiles { it.toAabFileType() }
        val manifest = this[MANIFEST_FILE_PATH].asInput().source().use {
          XmlNode.parseFrom(it.inputStream()).toManifest()
        }
        val dexes = entries.filter { it.path.startsWith("dex/") }.map { it.asInput().toDex() }
        return Module(files, manifest, dexes)
      }
    }
  }

  companion object {
    private const val BUNDLE_METADATA_DIRECTORY_NAME = "BUNDLE-METADATA"
    private const val METADATA_OBFUSCATION_DIRECTORY_NAME =
      "$BUNDLE_METADATA_DIRECTORY_NAME/com.android.tools.build.obfuscation/proguard.map"
    private const val BASE_DIRECTORY_NAME = "base"

    @JvmStatic
    @JvmName("parse")
    fun Input.toAab(): Aab {
      toZip().use { zip ->
        val apiMapping =
          zip.find(METADATA_OBFUSCATION_DIRECTORY_NAME)?.asInput()?.toApiMapping() ?: ApiMapping.EMPTY
        val baseModule = zip.directoryView(BASE_DIRECTORY_NAME).toModule()
        val featureModules = zip.directories
          // TODO there's probably a better way to discover feature module names.
          .filter { it != BASE_DIRECTORY_NAME && it != BUNDLE_METADATA_DIRECTORY_NAME && it != "META-INF" }
          .associateWith { zip.directoryView(it).toModule() }
        return Aab(name, apiMapping, baseModule, featureModules)
      }
    }

    private val Zip.directories: List<String> get() {
      return entries.mapNotNullTo(LinkedHashSet()) {
        val slash = it.path.indexOf('/')
        if (slash == -1) {
          null
        } else {
          it.path.substring(0, slash)
        }
      }.sorted()
    }
  }
}

package com.jakewharton.diffuse

import com.android.aapt.Resources.XmlNode
import com.jakewharton.diffuse.Aab.Module.Companion.toModule
import com.jakewharton.diffuse.ApiMapping.Companion.toApiMapping
import com.jakewharton.diffuse.ArchiveFile.Type.Companion.toAabFileType
import com.jakewharton.diffuse.ArchiveFiles.Companion.toArchiveFiles
import com.jakewharton.diffuse.Dex.Companion.toDex
import com.jakewharton.diffuse.Manifest.Companion.toManifest
import com.jakewharton.diffuse.io.Input
import com.jakewharton.diffuse.io.Zip

class Aab private constructor(
  override val filename: String?,
  val apiMapping: ApiMapping,
  val baseModule: Module,
  val featureModules: Map<String, Module>
) : Binary {
  class Module private constructor(
    val files: ArchiveFiles,
    val manifest: Manifest,
    val dexes: List<Dex>
  ) {
    companion object {
      internal const val manifestFilePath = "manifest/${Apk.manifestFileName}"

      fun Zip.toModule(): Module {
        val files = this.toArchiveFiles { it.toAabFileType() }
        val manifest = this[manifestFilePath].asInput().source().use {
          XmlNode.parseFrom(it.inputStream()).toManifest()
        }
        val dexes = entries.filter { it.path.startsWith("dex/") }.map { it.asInput().toDex() }
        return Module(files, manifest, dexes)
      }
    }
  }

  companion object {
    private const val bundleMetadataDirectoryName = "BUNDLE-METADATA"
    private const val metadataObfuscationDirectoryName =
      "$bundleMetadataDirectoryName/com.android.tools.build.obfuscation/proguard.map"
    private const val baseDirectoryName = "base"

    @JvmStatic
    @JvmName("parse")
    fun Input.toAab(): Aab {
      toZip().use { zip ->
        val apiMapping =
          zip.find(metadataObfuscationDirectoryName)?.asInput()?.toApiMapping() ?: ApiMapping.EMPTY
        val baseModule = zip.directoryView(baseDirectoryName).toModule()
        val featureModules = zip.directories
            // TODO there's probably a better way to discover feature module names.
            .filter { it != baseDirectoryName && it != bundleMetadataDirectoryName && it != "META-INF" }
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

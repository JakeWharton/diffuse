package com.jakewharton.diffuse.diff

import com.jakewharton.diffuse.format.Aab
import com.jakewharton.diffuse.report.Report
import com.jakewharton.diffuse.report.text.AabDiffTextReport

internal class AabDiff(
  val oldAab: Aab,
  val newAab: Aab,
) : BinaryDiff {
  inner class ModuleDiff(
    val oldModule: Aab.Module,
    val newModule: Aab.Module,
  ) {
    val archive = ArchiveFilesDiff(oldModule.files, newModule.files, includeCompressed = false)
    val dex = DexDiff(
      oldModule.dexes.map { it.withMapping(oldAab.apiMapping) },
      newModule.dexes.map { it.withMapping(newAab.apiMapping) },
    )
    val manifest = ManifestDiff(oldModule.manifest, newModule.manifest)

    val changed = archive.changed || dex.changed || manifest.changed
  }

  val baseModule = ModuleDiff(oldAab.baseModule, newAab.baseModule)
  val featureModuleNames = (oldAab.featureModules.keys + newAab.featureModules.keys).sorted()

  val addedFeatureModules = newAab.featureModules.filterKeys { it !in oldAab.featureModules }
  val removedFeatureModules = oldAab.featureModules.filterKeys { it !in newAab.featureModules }
  val changedFeatureModules = oldAab.featureModules.filterKeys { it in newAab.featureModules }
    .mapValues { (name, oldModule) ->
      ModuleDiff(oldModule, newAab.featureModules.getValue(name))
    }

  override fun toTextReport(): Report = AabDiffTextReport(this)
}

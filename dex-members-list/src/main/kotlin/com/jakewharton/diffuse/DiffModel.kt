package com.jakewharton.diffuse

import com.jakewharton.dex.ApiMapping
import com.jakewharton.dex.DexField
import com.jakewharton.dex.DexMethod

internal class ApkDiff(
  val oldApk: Apk,
  val oldMapping: ApiMapping,
  val newApk: Apk,
  val newMapping: ApiMapping
) : Diff {
  val archive = ArchiveDiff(oldApk.files, newApk.files)
  val dex = DexDiff(oldApk.dexes, oldMapping, newApk.dexes, newMapping)
  val arsc = ArscDiff(oldApk.arsc, newApk.arsc)

  override fun toTextReport(): DiffReport = ApkDiffTextReport(this)
}

internal class ArchiveDiff(
  val oldFiles: List<ArchiveFile>,
  val newFiles: List<ArchiveFile>
)

internal class DexDiff(
  val oldDexes: List<Dex>,
  val oldMapping: ApiMapping,
  val newDexes: List<Dex>,
  val newMapping: ApiMapping
) {
  val isMultidex = oldDexes.size > 1 || newDexes.size > 1

  val strings = componentDiff { it.strings }
  val types = componentDiff { it.types }
  val classes = componentDiff { it.classes }
  val methods = componentDiff { it.members.filterIsInstance<DexMethod>() }
  val declaredMethods = componentDiff { it.declaredMembers.filterIsInstance<DexMethod>() }
  val referencedMethods = componentDiff { it.referencedMembers.filterIsInstance<DexMethod>() }
  val fields = componentDiff { it.members.filterIsInstance<DexMethod>() }
  val declaredFields = componentDiff { it.declaredMembers.filterIsInstance<DexField>() }
  val referencedFields = componentDiff { it.referencedMembers.filterIsInstance<DexField>() }

  private fun <T> componentDiff(selector: (Dex) -> Collection<T>): ComponentDiff<T> {
    val oldRawCount = oldDexes.sumBy { selector(it).size }
    val newRawCount = newDexes.sumBy { selector(it).size }
    val old = oldDexes.flatMapTo(mutableSetOf(), selector)
    val new = newDexes.flatMapTo(mutableSetOf(), selector)
    val added = new - old
    val removed = old - new
    return ComponentDiff(oldRawCount, old.size, newRawCount, new.size, added, removed)
  }

  class ComponentDiff<T>(
    val oldRawCount: Int,
    val oldCount: Int,
    val newRawCount: Int,
    val newCount: Int,
    val added: Set<T>,
    val removed: Set<T>
  )
}

internal class ArscDiff(
  val oldArsc: Arsc,
  val newArsc: Arsc
) {
  val configsAdded = (newArsc.configs - oldArsc.configs).sorted()
  val configsRemoved = (oldArsc.configs - newArsc.configs).sorted()
  val entriesAdded = (newArsc.entries - oldArsc.entries).sorted()
  val entriesRemoved = (oldArsc.entries - newArsc.entries).sorted()
}

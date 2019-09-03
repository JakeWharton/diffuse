package com.jakewharton.diffuse

import com.jakewharton.dex.ApiMapping
import com.jakewharton.dex.DexField
import com.jakewharton.dex.DexMethod
import java.util.SortedMap

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
  val oldFiles: SortedMap<String, ArchiveFile>,
  val newFiles: SortedMap<String, ArchiveFile>
) {
  data class Change(
    val path: String,
    val sizeDiff: Size,
    val uncompressedSizeDiff: Size,
    val type: Type
  ) {
    enum class Type { Added, Removed, Changed }
  }

  val changes: List<Change>
  init {
    val added = newFiles.mapNotNull { (path, newFile) ->
      if (path !in oldFiles) {
        Change(path, newFile.size, newFile.uncompressedSize, Change.Type.Added)
      } else {
        null
      }
    }
    val removed = oldFiles.mapNotNull { (path, oldFile) ->
      if (path !in newFiles) {
        Change(path, -oldFile.size, -oldFile.uncompressedSize, Change.Type.Removed)
      } else {
        null
      }
    }
    val changed = oldFiles.mapNotNull { (path, oldFile) ->
      val newFile = newFiles[path]
      if (newFile != null && newFile != oldFile) {
        Change(path, newFile.size - oldFile.size,
            newFile.uncompressedSize - oldFile.uncompressedSize, Change.Type.Changed)
      } else {
        null
      }
    }
    changes = (added + removed + changed).sortedByDescending { it.sizeDiff.absoluteValue }
  }

  val changed = oldFiles != newFiles
}

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
  val fields = componentDiff { it.members.filterIsInstance<DexField>() }
  val declaredFields = componentDiff { it.declaredMembers.filterIsInstance<DexField>() }
  val referencedFields = componentDiff { it.referencedMembers.filterIsInstance<DexField>() }

  val changed = strings.changed || types.changed || methods.changed || fields.changed

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
  ) {
    val changed get() = added.isNotEmpty() || removed.isNotEmpty()
  }
}

internal class ArscDiff(
  val oldArsc: Arsc,
  val newArsc: Arsc
) {
  val configsAdded = (newArsc.configs - oldArsc.configs).sorted()
  val configsRemoved = (oldArsc.configs - newArsc.configs).sorted()
  val entriesAdded = (newArsc.entries - oldArsc.entries).sorted()
  val entriesRemoved = (oldArsc.entries - newArsc.entries).sorted()

  val changed = configsAdded.isNotEmpty() ||
      configsRemoved.isNotEmpty() ||
      entriesAdded.isNotEmpty() ||
      entriesRemoved.isNotEmpty()
}

package com.jakewharton.diffuse.diff

import com.jakewharton.diffuse.diffuseTable
import com.jakewharton.diffuse.report.toDiffString
import com.jakewharton.picnic.TextAlignment.MiddleRight
import com.jakewharton.picnic.renderText

/**
 * Diff for versioned items across two sets of containers.
 *
 * @param versionCounts per-version summary: maps each version to a pair of (`oldCount`, `newCount`)
 *   where the counts differ, sorted by `version`.
 * @param changedItems items that exist in both old and new but changed version, as triples of
 *   (`item`, `oldVersion`, `newVersion`), sorted by `item`.
 */
internal class VersionDiff<K : Comparable<K>, V : Comparable<V>>(
  val versionCounts: Map<V, Pair<Int, Int>>,
  val changedItems: List<Triple<K, V, V>>,
) {
  val changed = versionCounts.isNotEmpty() || changedItems.isNotEmpty()
}

internal fun <R, K : Comparable<K>, V : Comparable<V>> versionDiff(
  oldItems: List<R>,
  newItems: List<R>,
  selector: (R) -> Map<K, V>,
): VersionDiff<K, V> {
  val oldVersionMap = buildMap {
    for (item in oldItems) {
      putAll(selector(item))
    }
  }
  val newVersionMap = buildMap {
    for (item in newItems) {
      putAll(selector(item))
    }
  }

  // Items present in both with different versions.
  val changedItems = buildList {
    for ((item, oldVersion) in oldVersionMap) {
      val newVersion = newVersionMap[item]
      if (newVersion != null && newVersion != oldVersion) {
        add(Triple(item, oldVersion, newVersion))
      }
    }
  }
    .sortedBy { it.first }

  // Tally per-version counts across all items in old and new.
  val oldCountMap = oldVersionMap.values.groupingBy { it }.eachCount()
  val newCountMap = newVersionMap.values.groupingBy { it }.eachCount()
  val allVersions = (oldCountMap.keys + newCountMap.keys).toSortedSet()
  val versionCounts = buildMap {
    for (version in allVersions) {
      val oldCount = oldCountMap.getOrDefault(version, 0)
      val newCount = newCountMap.getOrDefault(version, 0)
      if (oldCount != newCount) {
        put(version, oldCount to newCount)
      }
    }
  }

  return VersionDiff(versionCounts, changedItems)
}

internal fun StringBuilder.appendVersionDiff(name: String, diff: VersionDiff<*, *>) {
  if (!diff.changed) return
  appendLine()
  appendLine("$name:")
  appendLine()

  if (diff.versionCounts.isNotEmpty()) {
    diffuseTable {
        header {
          row {
            cell("version")
            cell("old")
            cell("new")
            cell("diff")
          }
        }

        body {
          cellStyle { alignment = MiddleRight }

          for ((version, counts) in diff.versionCounts) {
            val (oldCount, newCount) = counts
            val delta = newCount - oldCount
            val net = delta.toDiffString()
            val added = delta.coerceAtLeast(0).toDiffString(zeroSign = '+')
            val removed = delta.coerceAtMost(0).toDiffString(zeroSign = '-')
            row(version, oldCount, newCount, "$net ($added $removed)")
          }
        }
      }
      .renderText()
      .prependIndent("  ")
      .let(::appendLine)
  }

  if (diff.changedItems.isNotEmpty()) {
    if (diff.versionCounts.isNotEmpty()) {
      appendLine()
    }
    for ((item, oldVersion, newVersion) in diff.changedItems) {
      appendLine("  $item: $oldVersion → $newVersion")
    }
  }
}

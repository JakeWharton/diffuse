package com.jakewharton.diffuse

import de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment

class ArscDiff(
  val oldArsc: Arsc,
  val newArsc: Arsc
) {
  fun toTextReport() = asciiTable {
    val header = addRow("ARSC", "old", "new", "diff")
    addRule()

    val oldConfigs = oldArsc.configs
    val newConfigs = newArsc.configs
    val configsAdded = newConfigs - oldConfigs
    val configsRemoved = oldConfigs - newConfigs
    val configsDelta = configsAdded.size - configsRemoved.size
    val configsDiff = buildString {
      append(configsDelta.toDiffString())
      if (configsDelta > 0) {
        append(" (")
        append(configsAdded.size.toDiffString())
        append(' ')
        append((-configsRemoved.size).toDiffString())
        append(')')
      }
    }
    addRow("configs", oldConfigs.size, newConfigs.size, configsDiff)

    val oldEntries = oldArsc.entries
    val newEntries = newArsc.entries
    val entriesAdded = newEntries - oldEntries
    val entriesRemoved = oldEntries - newEntries
    val entriesDelta = entriesAdded.size - entriesRemoved.size
    val entriesDiff = buildString {
      append(entriesDelta.toDiffString())
      if (entriesDelta > 0) {
        append(" (")
        append(entriesAdded.size.toDiffString())
        append(' ')
        append((-entriesRemoved.size).toDiffString())
        append(')')
      }
    }
    addRow("entries", oldEntries.size, newEntries.size, entriesDiff)

    setPaddingLeftRight(1)
    setTextAlignment(TextAlignment.RIGHT)
    header.setTextAlignment(TextAlignment.LEFT)
  }
}

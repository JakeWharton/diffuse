package com.jakewharton.diffuse

import com.jakewharton.dex.ApiMapping
import com.jakewharton.dex.DexField
import com.jakewharton.dex.DexMethod
import de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment

class DexDiff(
    val oldDexes: List<Dex>,
    val oldMapping: ApiMapping = ApiMapping.EMPTY,
    val newDexes: List<Dex>,
    val newMapping: ApiMapping = ApiMapping.EMPTY
) {
  /** True if either [oldDexes] or [newDexes] is larger than 1. */
  val isMultidex get() = oldDexes.size > 1 || newDexes.size > 1

  fun toTextReport() = asciiTable {
    val header = if (isMultidex) {
      val header = addRow("DEX", "old", "new", "diff", "old", "new", "diff")
      addRule()
      addRow("count", oldDexes.size, newDexes.size,
          (newDexes.size - oldDexes.size).toDiffString(), "", "", "")
      header
    } else {
      val header = addRow("DEX", "old", "new", "diff")
      addRule()
      addRow("count", oldDexes.size, newDexes.size,
          (newDexes.size - oldDexes.size).toDiffString())
      header
    }

    fun addDexRow(name: String, selector: (Dex) -> Collection<Any>) {
      val oldSize = oldDexes.sumBy { selector(it).size }
      val newSize = newDexes.sumBy { selector(it).size }
      val diff = (newSize - oldSize).toDiffString()

      if (!isMultidex) {
        addRow(name, oldSize, newSize, diff)
      } else {
        val oldUnique = oldDexes.flatMapTo(mutableSetOf(), selector)
        val newUnique = newDexes.flatMapTo(mutableSetOf(), selector)
        val added = newUnique - oldUnique
        val removed = oldUnique - newUnique
        val addedSize = added.size.toDiffString()
        val removedSize = (-removed.size).toDiffString()
        val finalDiff = (added.size - removed.size).toDiffString()
        addRow(name, oldSize, newSize, diff, oldUnique.size, newUnique.size,
            "$finalDiff ($addedSize $removedSize)")
      }
    }

    addDexRow("strings") { it.strings }
    addDexRow("types") { it.types }
    addDexRow("classes") { it.classes }
    addDexRow("methods") { it.members.filterIsInstance<DexMethod>() }
    addDexRow("fields") { it.members.filterIsInstance<DexField>() }

    setPaddingLeftRight(1)
    setTextAlignment(TextAlignment.RIGHT)
    header.setTextAlignment(TextAlignment.LEFT)
  }
}

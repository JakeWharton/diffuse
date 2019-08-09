package com.jakewharton.diffuse

import com.jakewharton.diffuse.ArchiveFile.Type
import de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment

class ArchiveDiff(
  val oldFiles: List<ArchiveFile>,
  val newFiles: List<ArchiveFile>
) {
  fun toTextReport() = asciiTable {
    // TODO inject name here?
    val header = addRow("APK", "old", "new", "diff", "old (u)", "new (u)", "diff (u)")
    addRule()

    fun addApkRow(name: String, type: Type? = null) {
      val old = if (type != null) oldFiles.filter { it.type == type } else oldFiles
      val new = if (type != null) newFiles.filter { it.type == type } else newFiles
      val oldSize = old.fold(Size.ZERO) { acc, file -> acc + file.size }
      val newSize = new.fold(Size.ZERO) { acc, file -> acc + file.size }
      val oldUncompressedSize = old.fold(Size.ZERO) { acc, file -> acc + file.size }
      val newUncompressedSize = new.fold(Size.ZERO) { acc, file -> acc + file.size }
      addRow(name, oldSize, newSize, (newSize - oldSize).toDiffString(), oldUncompressedSize,
          newUncompressedSize, (newUncompressedSize - oldUncompressedSize).toDiffString())
    }

    addApkRow("dex", Type.Dex)
    addApkRow("arsc", Type.Arsc)
    addApkRow("manifest", Type.Manifest)
    addApkRow("res", Type.Res)
    addApkRow("native", Type.Native)
    addApkRow("asset", Type.Asset)
    addApkRow("other", Type.Other)
    addRule()
    addApkRow("total")

    setPaddingLeftRight(1)
    setTextAlignment(TextAlignment.RIGHT)
    header.setTextAlignment(TextAlignment.LEFT)
  }
}

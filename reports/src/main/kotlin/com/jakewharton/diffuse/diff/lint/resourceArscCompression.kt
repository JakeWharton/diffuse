package com.jakewharton.diffuse.diff.lint

import com.jakewharton.diffuse.diff.ArchiveFilesDiff
import com.jakewharton.diffuse.diff.lint.Notice.Type
import com.jakewharton.diffuse.format.Arsc

internal fun ArchiveFilesDiff.resourcesArscCompression(): Notice? {
  val oldCompressed = oldFiles[Arsc.NAME]!!.isCompressed
  val newCompressed = newFiles[Arsc.NAME]!!.isCompressed
  return when {
    !newCompressed && !oldCompressed -> null
    newCompressed -> Notice(
      "resources.arsc changed from correctly uncompressed to incorrectly compressed",
      Type.Warning,
    )
    oldCompressed -> Notice(
      "resources.arsc changed from incorrectly compressed to correctly uncompressed",
      Type.Resolution,
    )
    else -> Notice(
      "resources.arsc remains incorrectly compressed instead of correctly uncompressed",
      Type.Warning,
    )
  }
}

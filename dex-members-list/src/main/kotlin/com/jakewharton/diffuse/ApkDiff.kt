package com.jakewharton.diffuse

import com.jakewharton.dex.ApiMapping
import de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment

class ApkDiff(
  val oldApk: Apk,
  val oldMapping: ApiMapping = ApiMapping.EMPTY,
  val newApk: Apk,
  val newMapping: ApiMapping = ApiMapping.EMPTY
) {
  val archive: ArchiveDiff by lazy { ArchiveDiff(oldApk.files, newApk.files) }
  val dex: DexDiff by lazy { DexDiff(oldApk.dexes, oldMapping, newApk.dexes, newMapping) }
  val arsc: ArscDiff by lazy { ArscDiff(oldApk.arsc, newApk.arsc) }

  fun toTextReport() = buildString {
    appendln(asciiTable {
      addRow("OLD/NEW", oldApk.filename, newApk.filename)
      addRule()
      addRow("md5", oldApk.bytes.md5().hex(), newApk.bytes.md5().hex())
      addRow("sha1", oldApk.bytes.sha1().hex(), newApk.bytes.sha1().hex())
      addRow("mapping", oldMapping.toSummaryString(), newMapping.toSummaryString())

      setPaddingLeftRight(1)
      setTextAlignment(TextAlignment.LEFT)
    })
    appendln()
    appendln()
    appendln(archive.toTextReport())
    appendln()
    appendln()
    appendln(dex.toTextReport())
    appendln()
    appendln()
    appendln(arsc.toTextReport())
  }

  private fun ApiMapping.toSummaryString(): String {
    if (this === ApiMapping.EMPTY) {
      return "not provided"
    }
    return buildString {
      append(types.toUnitString("types", 1 to "type"))
      if (types > 0) {
        append(", ")
        append(fields.toUnitString("fields", 1 to "field"))
        append(", ")
        append(methods.toUnitString("methods", 1 to "method"))
      }
    }
  }
}

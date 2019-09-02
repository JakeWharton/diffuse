package com.jakewharton.diffuse

import com.jakewharton.dex.ApiMapping

class ApkDiff(
  val oldApk: Apk,
  val oldMapping: ApiMapping = ApiMapping.EMPTY,
  val newApk: Apk,
  val newMapping: ApiMapping = ApiMapping.EMPTY
) : Diff {
  val archive: ArchiveDiff by lazy { ArchiveDiff(oldApk.files, newApk.files) }
  val dex: DexDiff by lazy { DexDiff(oldApk.dexes, oldMapping, newApk.dexes, newMapping) }
  val arsc: ArscDiff by lazy { ArscDiff(oldApk.arsc, newApk.arsc) }

  override fun toTextReport() = buildString {
    appendln(diffuseTable {
      header {
        row("OLD/NEW", oldApk.filename, newApk.filename)
      }
      body {
        row("md5", oldApk.bytes.md5().hex(), newApk.bytes.md5().hex())
        row("sha1", oldApk.bytes.sha1().hex(), newApk.bytes.sha1().hex())
        row("mapping", oldMapping.toSummaryString(), newMapping.toSummaryString())
      }
    })
    appendln()
    appendln(archive.toTextReport())
    appendln()
    appendln(dex.toTextReport())
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

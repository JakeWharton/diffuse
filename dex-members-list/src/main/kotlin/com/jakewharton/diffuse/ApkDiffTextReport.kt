package com.jakewharton.diffuse

import com.jakewharton.dex.ApiMapping

internal class ApkDiffTextReport(private val apkDiff: ApkDiff) : DiffReport {
  override fun write(appendable: Appendable) {
    with(appendable) {
      appendln(diffuseTable {
        header {
          row("OLD/NEW", apkDiff.oldApk.filename, apkDiff.newApk.filename)
        }
        body {
          row("md5", apkDiff.oldApk.bytes.md5().hex(), apkDiff.newApk.bytes.md5().hex())
          row("sha1", apkDiff.oldApk.bytes.sha1().hex(), apkDiff.newApk.bytes.sha1().hex())
          row("mapping", apkDiff.oldMapping.toSummaryString(), apkDiff.newMapping.toSummaryString())
        }
      }.toString())
      appendln()
      appendln(apkDiff.archive.toTextReport())
      appendln()
      appendln(apkDiff.dex.toTextReport())
      appendln()
      appendln(apkDiff.arsc.toTextReport())
    }
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

  override fun toString() = buildString { write(this) }
}

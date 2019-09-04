package com.jakewharton.diffuse

internal class ApkDiffTextReport(private val apkDiff: ApkDiff) : DiffReport {
  override fun write(appendable: Appendable) {
    appendable.apply {
      appendln(diffuseTable {
        header {
          row("OLD/NEW", apkDiff.oldApk.filename, apkDiff.newApk.filename)
        }
        body {
          row("md5", apkDiff.oldApk.bytes.md5().hex(), apkDiff.newApk.bytes.md5().hex())
          row("sha1", apkDiff.oldApk.bytes.sha1().hex(), apkDiff.newApk.bytes.sha1().hex())
          row("signature", apkDiff.oldApk.signatures.toSummaryString(), apkDiff.newApk.signatures.toSummaryString())
          row("mapping", apkDiff.oldMapping.toSummaryString(), apkDiff.newMapping.toSummaryString())
        }
      }.toString())
      appendln()
      appendln(apkDiff.archive.toSummaryTable("APK"))
      appendln()
      appendln(apkDiff.dex.toSummaryTable())
      appendln()
      appendln(apkDiff.arsc.toSummaryTable())
      if (apkDiff.archive.changed || apkDiff.signatures.changed) {
        appendln()
        appendln("=================")
        appendln("====   APK   ====")
        appendln("=================")
        if (apkDiff.archive.changed) {
          appendln(apkDiff.archive.toDetailReport())
        }
        if (apkDiff.signatures.changed) {
          appendln(apkDiff.signatures.toDetailReport())
        }
      }
      if (apkDiff.dex.changed) {
        appendln()
        appendln("=================")
        appendln("====   DEX   ====")
        appendln("=================")
        appendln(apkDiff.dex.toDetailReport())
      }
      if (apkDiff.arsc.changed) {
        appendln()
        appendln("==================")
        appendln("====   ARSC   ====")
        appendln("==================")
        appendln(apkDiff.arsc.toDetailReport())
      }
    }
  }

  override fun toString() = buildString { write(this) }
}

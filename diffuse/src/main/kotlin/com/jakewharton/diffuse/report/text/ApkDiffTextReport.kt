package com.jakewharton.diffuse.report.text

import com.jakewharton.diffuse.ArchiveFile.Type
import com.jakewharton.diffuse.diff.ApkDiff
import com.jakewharton.diffuse.diff.toDetailReport
import com.jakewharton.diffuse.diff.toSummaryTable
import com.jakewharton.diffuse.diffuseTable
import com.jakewharton.diffuse.report.DiffReport
import com.jakewharton.diffuse.report.toSummaryString

internal class ApkDiffTextReport(private val apkDiff: ApkDiff) : DiffReport {
  override fun write(appendable: Appendable) {
    appendable.apply {
      appendln(diffuseTable {
        header {
          row("OLD/NEW", apkDiff.oldApk.filename, apkDiff.newApk.filename)
        }
        body {
          // TODO do we care about showing a hash?
          //  row("md5", apkDiff.oldApk.bytes.md5().hex(), apkDiff.newApk.bytes.md5().hex())
          //  row("sha1", apkDiff.oldApk.bytes.sha1().hex(), apkDiff.newApk.bytes.sha1().hex())
          row("signature", apkDiff.oldApk.signatures.toSummaryString(), apkDiff.newApk.signatures.toSummaryString())
          row("mapping", apkDiff.oldMapping.toSummaryString(), apkDiff.newMapping.toSummaryString())
        }
      }.toString())
      appendln()
      if (apkDiff.lintMessages.isNotEmpty()) {
        appendln("NOTICE:")
        apkDiff.lintMessages.forEach {
          append(" · ")
          appendln(it)
        }
        appendln()
        appendln()
      }
      appendln(apkDiff.archive.toSummaryTable("APK", Type.APK_TYPES,
          skipIfEmptyTypes = setOf(Type.Native)))
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
      if (apkDiff.manifest.changed) {
        appendln()
        appendln("======================")
        appendln("====   MANIFEST   ====")
        appendln("======================")
        appendln(apkDiff.manifest.toDetailReport())
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

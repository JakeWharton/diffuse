package com.jakewharton.diffuse.report.text

import com.jakewharton.diffuse.diff.AabDiff
import com.jakewharton.diffuse.diffuseTable
import com.jakewharton.diffuse.report.DiffReport

internal class AabDiffTextReport(private val aabDiff: AabDiff) : DiffReport {
  override fun write(appendable: Appendable) {
    appendable.apply {
      appendln(diffuseTable {
        header {
          row("OLD/NEW", aabDiff.oldAab.filename, aabDiff.newAab.filename)
        }
        body {
          // TODO do we care about showing a hash?
          //  row("md5", jarDiff.oldJar.bytes.md5().hex(), jarDiff.newJar.bytes.md5().hex())
          //  row("sha1", jarDiff.oldJar.bytes.sha1().hex(), jarDiff.newJar.bytes.sha1().hex())
        }
      }.toString())
      appendln()
    }
  }

  override fun toString() = buildString { write(this) }
}

package com.jakewharton.diffuse

internal class AarDiffTextReport(private val aarDiff: AarDiff) : DiffReport {
  override fun write(appendable: Appendable) {
    appendable.apply {
      appendln(diffuseTable {
        header {
          row("OLD/NEW", aarDiff.oldAar.filename, aarDiff.newAar.filename)
        }
        body {
          row("md5", aarDiff.oldAar.bytes.md5().hex(), aarDiff.newAar.bytes.md5().hex())
          row("sha1", aarDiff.oldAar.bytes.sha1().hex(), aarDiff.newAar.bytes.sha1().hex())
          row("mapping", aarDiff.oldMapping.toSummaryString(), aarDiff.newMapping.toSummaryString())
        }
      }.toString())
      appendln()
      appendln(aarDiff.archive.toSummaryTable("AAR"))
      if (aarDiff.archive.changed) {
        appendln()
        appendln("=================")
        appendln("====   AAR   ====")
        appendln("=================")
        appendln(aarDiff.archive.toDetailReport())
      }
    }
  }

  override fun toString() = buildString { write(this) }
}

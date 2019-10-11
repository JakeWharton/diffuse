package com.jakewharton.diffuse

import com.jakewharton.diffuse.ArchiveFile.Type

internal class JarDiffTextReport(private val jarDiff: JarDiff) : DiffReport {
  override fun write(appendable: Appendable) {
    appendable.apply {
      appendln(diffuseTable {
        header {
          row("OLD/NEW", jarDiff.oldJar.filename, jarDiff.newJar.filename)
        }
        body {
          // TODO do we care about showing a hash?
          //  row("md5", jarDiff.oldJar.bytes.md5().hex(), jarDiff.newJar.bytes.md5().hex())
          //  row("sha1", jarDiff.oldJar.bytes.sha1().hex(), jarDiff.newJar.bytes.sha1().hex())
          row("mapping", jarDiff.oldMapping.toSummaryString(), jarDiff.newMapping.toSummaryString())
        }
      }.toString())
      appendln()
      appendln(jarDiff.archive.toSummaryTable("JAR", Type.JAR_TYPES))
      if (jarDiff.archive.changed) {
        appendln()
        appendln("=================")
        appendln("====   JAR   ====")
        appendln("=================")
        appendln(jarDiff.archive.toDetailReport())
      }
    }
  }

  override fun toString() = buildString { write(this) }
}

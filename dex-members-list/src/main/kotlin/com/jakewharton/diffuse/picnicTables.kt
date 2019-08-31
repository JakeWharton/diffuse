package com.jakewharton.diffuse

import com.jakewharton.picnic.SectionDsl
import com.jakewharton.picnic.Table
import com.jakewharton.picnic.TableDsl
import com.jakewharton.picnic.table

fun diffuseTable(content: TableDsl.() -> Unit): Table = table {
  cellStyle {
    paddingLeft = 1
    paddingRight = 1
    borderLeft = true
    borderRight = true
  }
  DiffuseTableDsl(this).content()
}

private class DiffuseTableDsl(private val delegate: TableDsl): TableDsl by delegate {
  override fun header(content: SectionDsl.() -> Unit) {
    delegate.header {
      cellStyle {
        borderBottom = true
      }
      content()
    }
  }

  override fun footer(content: SectionDsl.() -> Unit) {
    delegate.footer {
      cellStyle {
        borderTop = true
      }
      content()
    }
  }
}

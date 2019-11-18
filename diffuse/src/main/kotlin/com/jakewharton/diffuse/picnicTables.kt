package com.jakewharton.diffuse

import com.jakewharton.picnic.BorderStyle
import com.jakewharton.picnic.Table
import com.jakewharton.picnic.TableDsl
import com.jakewharton.picnic.TableSectionDsl
import com.jakewharton.picnic.table

internal fun diffuseTable(content: TableDsl.() -> Unit): Table = table {
  cellStyle {
    paddingLeft = 1
    paddingRight = 1
    borderLeft = true
    borderRight = true
  }
  style {
    borderStyle = BorderStyle.Hidden
  }
  DiffuseTableDsl(this).content()
}

private class DiffuseTableDsl(private val delegate: TableDsl) : TableDsl by delegate {
  override fun header(content: TableSectionDsl.() -> Unit) {
    delegate.header {
      cellStyle {
        borderBottom = true
      }
      content()
    }
  }

  override fun footer(content: TableSectionDsl.() -> Unit) {
    delegate.footer {
      cellStyle {
        borderTop = true
      }
      content()
    }
  }
}

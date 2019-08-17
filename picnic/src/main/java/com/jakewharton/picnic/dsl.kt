package com.jakewharton.picnic

@DslMarker
annotation class PicnicDsl

fun table(content: TableDsl.() -> Unit) = TableBuilder().apply(content).build()

@PicnicDsl
interface TableDsl : SectionDsl {
  fun header(content: SectionDsl.() -> Unit)
  fun body(content: SectionDsl.() -> Unit)
  fun footer(content: SectionDsl.() -> Unit)
}

@PicnicDsl
interface SectionDsl {
  fun row(vararg cells: Any?) {
    row {
      cells.forEach { cell(it) }
    }
  }

  fun row(content: RowDsl.() -> Unit)
}

@PicnicDsl
interface RowDsl {
  fun cell(content: Any?) {
    cell { content }
  }
  fun cell(content: CellDsl.() -> Any?)
}

@PicnicDsl
interface CellDsl {
  var columnSpan: Int
  var rowSpan: Int

  var paddingLeft: Int
  var paddingRight: Int
  var paddingTop: Int
  var paddingBottom: Int

  var borderLeft: Boolean
  var borderRight: Boolean
  var borderTop: Boolean
  var borderBottom: Boolean

  var alignment: TextAlignment

  var border: Boolean
    get() = borderLeft || borderRight || borderTop || borderBottom
    set(value) {
      borderLeft = value
      borderRight = value
      borderTop = value
      borderBottom = value
    }

  fun padding(
    left: Int = paddingLeft,
    right: Int = paddingRight,
    top: Int = paddingTop,
    bottom: Int = paddingBottom
  ) {
    paddingLeft = left
    paddingRight = right
    paddingTop = top
    paddingBottom = bottom
  }

  fun border(
    left: Boolean = borderLeft,
    right: Boolean = borderRight,
    top: Boolean = borderTop,
    bottom: Boolean = borderBottom
  ) {
    borderLeft = left
    borderRight = right
    borderTop = top
    borderBottom = bottom
  }
}

private class TableBuilder : TableDsl {
  private val headerBuilder = SectionBuilder()
  private val bodyBuilder = SectionBuilder()
  private val footerBuilder = SectionBuilder()

  override fun header(content: SectionDsl.() -> Unit) {
    headerBuilder.apply(content)
  }

  override fun body(content: SectionDsl.() -> Unit) {
    bodyBuilder.apply(content)
  }

  override fun footer(content: SectionDsl.() -> Unit) {
    footerBuilder.apply(content)
  }

  override fun row(content: RowDsl.() -> Unit) {
    bodyBuilder.row(content)
  }

  fun build() = Table(
      Header(headerBuilder.build()), Body(bodyBuilder.build()), Footer(footerBuilder.build()))
}

private class SectionBuilder : SectionDsl {
  private val rows = mutableListOf<Row>()

  override fun row(content: RowDsl.() -> Unit) {
    rows += RowBuilder().apply(content).build()
  }

  fun build() = rows.toList()
}

private class RowBuilder : RowDsl {
  private val cells = mutableListOf<Cell>()

  override fun cell(content: CellDsl.() -> Any?) {
    cells += CellBuilder().also { it.content = content(it) }.build()
  }

  fun build() = Row(cells.toList())
}

private class CellBuilder : CellDsl {
  private val unsetMarker = Any()
  var content: Any? = unsetMarker

  override var columnSpan: Int = 1
  override var rowSpan: Int = 1
  override var paddingLeft: Int = 0
  override var paddingRight: Int = 0
  override var paddingTop: Int = 0
  override var paddingBottom: Int = 0
  override var borderLeft: Boolean = false
  override var borderRight: Boolean = false
  override var borderTop: Boolean = false
  override var borderBottom: Boolean = false
  override var alignment: TextAlignment = TextAlignment.TopLeft

  fun build(): Cell {
    check(content !== unsetMarker) { "content property not set" }
    return Cell(
        content = content?.toString() ?: "",
        columnSpan = columnSpan,
        rowSpan = rowSpan,
        paddingLeft = paddingLeft,
        paddingRight = paddingRight,
        paddingTop = paddingTop,
        paddingBottom = paddingBottom,
        borderLeft = borderLeft,
        borderRight = borderRight,
        borderTop = borderTop,
        borderBottom = borderBottom,
        alignment = alignment
    )
  }
}

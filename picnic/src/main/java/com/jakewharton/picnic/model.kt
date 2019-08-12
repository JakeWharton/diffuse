package com.jakewharton.picnic

data class Table(val header: Header, val body: Body, val footer: Footer) {
  val positionedCells: List<PositionedCell> = mutableListOf<PositionedCell>().apply {
    val rowSpanCarries = IntCounts()
    val rows = header.rows + body.rows + footer.rows
    rows.forEachIndexed { rowIndex, row ->
      var columnIndex = 0
      row.cells.forEach { cell ->
        // Check for any previous rows' cells whose >1 rowSpan carries them into this row.
        // When found, advance the column index to avoid them, pushing remaining cells to the right.
        while (columnIndex < rowSpanCarries.size && rowSpanCarries[columnIndex] > 0) {
          rowSpanCarries[columnIndex++]--
        }

        this += PositionedCell(rowIndex, columnIndex, cell)

        val rowSpanCarry = cell.rowSpan - 1
        repeat(cell.columnSpan) {
          rowSpanCarries[columnIndex++] = rowSpanCarry
        }
      }
    }
  }

  override fun toString() = renderText()
}

interface TableSection {
  val rows: List<Row>
}
data class Header(override val rows: List<Row>) : TableSection
data class Body(override val rows: List<Row>): TableSection
data class Footer(override val rows: List<Row>): TableSection

data class Row(val cells: List<Cell>)

data class Cell(
  val content: String,
  val columnSpan: Int = 1,
  val rowSpan: Int = 1,
  val paddingLeft: Int = 0,
  val paddingRight: Int = 0,
  val paddingTop: Int = 0,
  val paddingBottom: Int = 0
)

data class PositionedCell(val rowIndex: Int, val columnIndex: Int, val cell: Cell)

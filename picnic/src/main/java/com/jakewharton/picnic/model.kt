package com.jakewharton.picnic

data class Table(val header: Header, val body: Body, val footer: Footer) {
  val rowCount: Int
  val columnCount: Int
  val positionedCells: List<PositionedCell>

  init {
    val rows = header.rows + body.rows + footer.rows
    rowCount = rows.size

    val rowSpanCarries = IntCounts()
    val cells = mutableListOf<PositionedCell>()
    rows.forEachIndexed { rowIndex, row ->
      var columnIndex = 0
      row.cells.forEachIndexed { rawRowIndex, cell ->
        // Check for any previous rows' cells whose >1 rowSpan carries them into this row.
        // When found, advance the column index to avoid them, pushing remaining cells to the right.
        while (columnIndex < rowSpanCarries.size && rowSpanCarries[columnIndex] > 0) {
          rowSpanCarries[columnIndex++]--
        }

        cells += PositionedCell(rowIndex, columnIndex, cell)

        val rowSpan = cell.rowSpan
        require(rowIndex + rowSpan <= rowCount) {
          "Cell $rawRowIndex in row $rowIndex has rowSpan=$rowSpan but table rowCount=$rowCount"
        }

        val rowSpanCarry = rowSpan - 1
        repeat(cell.columnSpan) {
          rowSpanCarries[columnIndex++] = rowSpanCarry
        }
      }
    }

    columnCount = rowSpanCarries.size
    positionedCells = cells
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

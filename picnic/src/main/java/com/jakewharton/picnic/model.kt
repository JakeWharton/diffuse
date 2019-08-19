package com.jakewharton.picnic

data class Table(val header: Header, val body: Body, val footer: Footer) {
  val rowCount: Int
  val columnCount: Int
  val positionedCells: List<PositionedCell>

  private val cellTable: List<List<PositionedCell>>

  init {
    val rows = header.rows + body.rows + footer.rows
    rowCount = rows.size

    val rowSpanCarries = IntCounts()
    val positionedCells = mutableListOf<PositionedCell>()
    val cellTable = mutableListOf<MutableList<PositionedCell>>()
    rows.forEachIndexed { rowIndex, row ->
      val cellRow = mutableListOf<PositionedCell>()
      cellTable += cellRow

      var columnIndex = 0
      row.cells.forEachIndexed { rawRowIndex, cell ->
        // Check for any previous rows' cells whose >1 rowSpan carries them into this row.
        // When found, add them to the current row, pushing remaining cells to the right.
        while (columnIndex < rowSpanCarries.size && rowSpanCarries[columnIndex] > 0) {
          cellRow += cellTable[rowIndex - 1][columnIndex]
          rowSpanCarries[columnIndex]--
          columnIndex++
        }

        val positionedCell = PositionedCell(rowIndex, columnIndex, cell)
        positionedCells += positionedCell

        val rowSpan = cell.rowSpan
        require(rowIndex + rowSpan <= rowCount) {
          "Cell $rawRowIndex in row $rowIndex has rowSpan=$rowSpan but table rowCount=$rowCount"
        }

        val rowSpanCarry = rowSpan - 1
        repeat(cell.columnSpan) {
          cellRow += positionedCell
          rowSpanCarries[columnIndex] = rowSpanCarry
          columnIndex++
        }
      }
    }

    columnCount = rowSpanCarries.size
    this.positionedCells = positionedCells
    this.cellTable = cellTable
  }

  fun getOrNull(row: Int, column: Int) = cellTable.getOrNull(row)?.getOrNull(column)

  operator fun get(row: Int, column: Int) = cellTable[row][column]

  override fun toString() = renderText()

  data class PositionedCell(val rowIndex: Int, val columnIndex: Int, val cell: Cell)
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
  val paddingBottom: Int = 0,
  val borderLeft: Boolean = false,
  val borderRight: Boolean = false,
  val borderTop: Boolean = false,
  val borderBottom: Boolean = false,
  val alignment: TextAlignment = TextAlignment.TopLeft
)

enum class TextAlignment {
  TopLeft, TopCenter, TopRight,
  MiddleLeft, MiddleCenter, MiddleRight,
  BottomLeft, BottomCenter, BottomRight
}

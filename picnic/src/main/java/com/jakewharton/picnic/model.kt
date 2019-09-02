package com.jakewharton.picnic

data class Table(
  val header: Header? = null,
  val body: Body,
  val footer: Footer? =  null,
  val cellStyle: CellStyle? = null,
  val tableStyle: TableStyle? = null
) {
  val rowCount: Int = (header?.rows?.size ?: 0) + body.rows.size + (footer?.rows?.size ?: 0)
  val columnCount: Int
  val positionedCells: List<PositionedCell>

  private val cellTable: List<List<PositionedCell?>>

  init {
    val rowSpanCarries = IntCounts()
    val positionedCells = mutableListOf<PositionedCell>()
    val cellTable = mutableListOf<MutableList<PositionedCell?>>()
    var rowIndex = 0
    listOfNotNull(header, body, footer).forEach { section ->
      val sectionStyle = cellStyle + section.cellStyle

      section.rows.forEach { row ->
        val rowStyle = sectionStyle + row.cellStyle

        val cellRow = mutableListOf<PositionedCell?>()
        cellTable += cellRow

        var columnIndex = 0
        row.cells.forEachIndexed { rawColumnIndex, cell ->
          // Check for any previous rows' cells whose >1 rowSpan carries them into this row.
          // When found, add them to the current row, pushing remaining cells to the right.
          while (columnIndex < rowSpanCarries.size && rowSpanCarries[columnIndex] > 0) {
            cellRow += cellTable[rowIndex - 1][columnIndex]
            rowSpanCarries[columnIndex]--
            columnIndex++
          }

          val canonicalStyle = rowStyle + cell.style
          val positionedCell = PositionedCell(rowIndex, columnIndex, cell, canonicalStyle)
          positionedCells += positionedCell

          val rowSpan = cell.rowSpan
          require(rowIndex + rowSpan <= rowCount) {
            "Cell $rawColumnIndex in row $rowIndex has rowSpan=$rowSpan but table rowCount=$rowCount"
          }

          val rowSpanCarry = rowSpan - 1
          repeat(cell.columnSpan) {
            cellRow += positionedCell
            rowSpanCarries[columnIndex] = rowSpanCarry
            columnIndex++
          }
        }

        // Check for any previous rows' cells whose >1 rowSpan carries them into this row.
        // When found, add them to the current row, filling any gaps with null.
        while (columnIndex < rowSpanCarries.size) {
          if (rowSpanCarries[columnIndex] > 0) {
            cellRow += cellTable[rowIndex - 1][columnIndex]
            rowSpanCarries[columnIndex]--
          } else {
            cellRow.add(null)
          }
          columnIndex++
        }

        rowIndex++
      }
    }

    columnCount = rowSpanCarries.size
    this.positionedCells = positionedCells
    this.cellTable = cellTable
  }

  fun getOrNull(row: Int, column: Int) = cellTable.getOrNull(row)?.getOrNull(column)

  operator fun get(row: Int, column: Int) = requireNotNull(cellTable[row][column]) {
    "Cell was null"
  }

  override fun toString() = renderText()

  data class PositionedCell(
    val rowIndex: Int,
    val columnIndex: Int,
    val cell: Cell,
    val canonicalStyle: CellStyle?
  )
}

data class TableStyle(
  val borderStyle: BorderStyle? = null
)

enum class BorderStyle {
  Hidden, Solid
}

interface TableSection {
  val rows: List<Row>
  val cellStyle: CellStyle?
}

data class Header(
  override val rows: List<Row>,
  override val cellStyle: CellStyle? = null
) : TableSection

data class Body(
  override val rows: List<Row>,
  override val cellStyle: CellStyle? = null
): TableSection

data class Footer(
  override val rows: List<Row>,
  override val cellStyle: CellStyle? = null
): TableSection

data class Row(val cells: List<Cell>, val cellStyle: CellStyle? = null)

data class Cell(
  val content: String,
  val columnSpan: Int = 1,
  val rowSpan: Int = 1,
  val style: CellStyle? = null
)

data class CellStyle(
  val paddingLeft: Int? = null,
  val paddingRight: Int? = null,
  val paddingTop: Int? = null,
  val paddingBottom: Int? = null,
  val borderLeft: Boolean? = null,
  val borderRight: Boolean? = null,
  val borderTop: Boolean? = null,
  val borderBottom: Boolean? = null,
  val alignment: TextAlignment? = null
)

private operator fun CellStyle?.plus(override: CellStyle?): CellStyle? {
  if (this == null) {
    return override
  }
  if (override == null) {
    return this
  }
  return CellStyle(
      paddingLeft = override.paddingLeft ?: paddingLeft,
      paddingRight = override.paddingRight ?: paddingRight,
      paddingTop = override.paddingTop ?: paddingTop,
      paddingBottom = override.paddingBottom ?: paddingBottom,
      borderLeft = override.borderLeft ?: borderLeft,
      borderRight = override.borderRight ?: borderRight,
      borderTop = override.borderTop ?: borderTop,
      borderBottom = override.borderBottom ?: borderBottom,
      alignment = override.alignment ?: alignment
  )
}

enum class TextAlignment {
  TopLeft, TopCenter, TopRight,
  MiddleLeft, MiddleCenter, MiddleRight,
  BottomLeft, BottomCenter, BottomRight
}

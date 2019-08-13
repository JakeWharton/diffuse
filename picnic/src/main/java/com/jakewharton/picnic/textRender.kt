@file:JvmName("TextRendering")

package com.jakewharton.picnic

@Suppress("NOTHING_TO_INLINE", "UNUSED_PARAMETER")
private inline fun debug(message: () -> String) {
  //println(message())
}

@JvmOverloads
@JvmName("render")
fun Table.renderText(layoutFactory: (Cell) -> TextLayout = ::SimpleLayout): String {
  val layouts = positionedCells.associate { it.cell to layoutFactory(it.cell) }

  debug { "Measure pass (1/2)..." }

  val columnWidths = IntArray(columnCount)
  val rowHeights = IntArray(rowCount)

  positionedCells.forEach { (rowIndex, columnIndex, cell) ->
    val layout = layouts.getValue(cell)

    if (cell.columnSpan == 1) {
      val currentWidth = columnWidths[columnIndex]
      val contentWidth = layout.measureWidth()
      if (contentWidth > currentWidth) {
        debug { " Increasing column $columnIndex width from $currentWidth to $contentWidth" }
        columnWidths[columnIndex] = contentWidth
      }
    }

    if (cell.rowSpan == 1) {
      val currentHeight = rowHeights[rowIndex]
      val contentHeight = layout.measureHeight()
      if (contentHeight > currentHeight) {
        debug { " Increasing row $rowIndex height from $currentHeight to $contentHeight" }
        rowHeights[rowIndex] = contentHeight
      }
    }
  }

  debug { "Measure pass (2/2)..." }

  positionedCells.filter { it.cell.columnSpan > 1 }
      .sortedBy { it.cell.columnSpan }
      .forEach { (_, columnIndex, cell) ->
        val layout = layouts.getValue(cell)
        val columnSpan = cell.columnSpan
        val contentWidth = layout.measureWidth()
        val columnSpanIndices = columnIndex until columnIndex + columnSpan
        val currentSpanWidth = columnSpanIndices.sumBy { columnWidths[it] }
        val remainingSize = contentWidth - currentSpanWidth
        if (remainingSize > 0) {
          // TODO change to distribute remaining size proportionally to the existing widths?
          val commonSize = remainingSize / columnSpan
          val extraSize = remainingSize - (commonSize * columnSpan)
          columnSpanIndices.forEachIndexed { spanIndex, targetColumnIndex ->
            val additionalSize = if (spanIndex < extraSize) {
              commonSize + 1
            } else {
              commonSize
            }
            val currentWidth = columnWidths[targetColumnIndex]
            val newWidth = currentWidth + additionalSize
            debug { " Increasing column $targetColumnIndex width from $currentWidth to $newWidth" }
            columnWidths[targetColumnIndex] = newWidth
          }
        }
      }

  positionedCells.filter { it.cell.rowSpan > 1 }
      .sortedBy { it.cell.rowSpan }
      .forEach { (rowIndex, _, cell) ->
        val layout = layouts.getValue(cell)
        val rowSpan = cell.rowSpan
        val contentHeight = layout.measureHeight()
        val rowSpanIndices = rowIndex until rowIndex + rowSpan
        val currentSpanHeight = rowSpanIndices.sumBy { rowHeights[it] }
        val remainingSize = contentHeight - currentSpanHeight
        if (remainingSize > 0) {
          // TODO change to distribute remaining size proportionally to the existing widths?
          val commonSize = remainingSize / rowSpan
          val extraSize = remainingSize - (commonSize * rowSpan)
          rowSpanIndices.forEachIndexed { spanIndex, targetRowIndex ->
            val additionalSize = if (spanIndex < extraSize) {
              commonSize + 1
            } else {
              commonSize
            }
            val currentHeight = rowHeights[targetRowIndex]
            val newHeight = currentHeight + additionalSize
            debug { " Increasing row $targetRowIndex height from $currentHeight to $newHeight" }
            rowHeights[targetRowIndex] = newHeight
          }
        }
      }

  debug { "Layout pass..." }

  val tableLefts = IntArray(columnWidths.size)
  val tableWidth: Int
  run {
    var left = 0
    for (i in 0 until columnWidths.size) {
      tableLefts[i] = left
      left += columnWidths[i]
    }
    tableWidth = left
  }

  val tableTops = IntArray(rowHeights.size)
  val tableHeight: Int
  run {
    var top = 0
    for (i in 0 until rowHeights.size) {
      tableTops[i] = top
      top += rowHeights[i]
    }
    tableHeight = top
  }

  debug { "Drawing pass..." }

  val surface = TextSurface(tableWidth, tableHeight)
  positionedCells.forEach { (rowIndex, columnIndex, cell) ->
    val cellLeft = tableLefts[columnIndex]
    val cellRight = tableLefts.getOrElse(columnIndex + cell.columnSpan) { tableWidth }
    val cellTop = tableTops[rowIndex]
    val cellBottom = tableTops.getOrElse(rowIndex + cell.rowSpan) { tableHeight }
    val canvas = surface.clip(cellLeft, cellRight, cellTop, cellBottom)

    val layout = layouts.getValue(cell)
    layout.draw(canvas)
  }
  return surface.toString()
}

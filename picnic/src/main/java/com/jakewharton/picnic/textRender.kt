@file:JvmName("TextRendering")

package com.jakewharton.picnic

private val String.width: Int get() {
  return split('\n').map { it.length }.max() ?: 0
}

private val String.height: Int get() = 1 + count { it == '\n' }

private fun Cell.textRender(canvas: TextCanvas) {
  canvas.print(content)
}

@Suppress("NOTHING_TO_INLINE", "UNUSED_PARAMETER")
private inline fun debug(message: () -> String) {
  //println(message())
}

@JvmName("render")
fun Table.renderText(): String {
  debug { "Measure pass (1/2)..." }

  val columnWidths = IntCounts()
  val rowHeights = IntCounts()

  positionedCells.forEach { (rowIndex, columnIndex, cell) ->
    val columnSpan = cell.columnSpan
    if (columnSpan == 1) {
      val currentWidth = columnWidths[columnIndex]
      val contentWidth = cell.content.width
      if (contentWidth > currentWidth) {
        debug { " Increasing column $columnIndex width from $currentWidth to $contentWidth" }
        columnWidths[columnIndex] = contentWidth
      }
    }

    val rowSpan = cell.rowSpan
    if (rowSpan == 1) {
      val currentHeight = rowHeights[rowIndex]
      val contentHeight = cell.content.height
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
        val columnSpan = cell.columnSpan
        val contentWidth = cell.content.width
        val columnSpanIndices = columnIndex until columnIndex + columnSpan
        val currentSpanWidth = columnSpanIndices.sumBy { columnWidths[columnIndex] }
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
        val rowSpan = cell.rowSpan
        val contentHeight = cell.content.height
        val rowSpanIndices = rowIndex until rowIndex + rowSpan
        val currentSpanHeight = rowSpanIndices.sumBy { rowHeights[rowIndex] }
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
    cell.textRender(canvas)
  }
  return surface.toString()
}

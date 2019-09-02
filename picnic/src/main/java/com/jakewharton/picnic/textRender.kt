@file:JvmName("TextRendering")

package com.jakewharton.picnic

import com.jakewharton.picnic.Table.PositionedCell

@Suppress("NOTHING_TO_INLINE", "UNUSED_PARAMETER")
private inline fun debug(message: () -> String) {
  //println(message())
}

@JvmOverloads
@JvmName("render")
fun Table.renderText(
  layoutFactory: (PositionedCell) -> TextLayout = ::SimpleLayout,
  border: TextBorder = TextBorder.DEFAULT
): String {
  val layouts = positionedCells.associate { it.cell to layoutFactory(it) }

  debug { "Measure pass...\n 1/2..." }

  val columnWidths = IntArray(columnCount) { 1 }
  val columnBorderWidths = IntArray(columnCount + 1)
  val rowHeights = IntArray(rowCount) { 1 }
  val rowBorderHeights = IntArray(rowCount + 1)

  positionedCells.forEach { (rowIndex, columnIndex, cell, canonicalStyle) ->
    val layout = layouts.getValue(cell)

    val columnSpan = cell.columnSpan
    if (columnSpan == 1) {
      val currentWidth = columnWidths[columnIndex]
      val contentWidth = layout.measureWidth()
      if (contentWidth > currentWidth) {
        debug { "  ($rowIndex, $columnIndex) Column width $currentWidth -> $contentWidth" }
        columnWidths[columnIndex] = contentWidth
      }
    }

    val rowSpan = cell.rowSpan
    if (rowSpan == 1) {
      val currentHeight = rowHeights[rowIndex]
      val contentHeight = layout.measureHeight()
      if (contentHeight > currentHeight) {
        debug { "  ($rowIndex, $columnIndex) Row height $currentHeight -> $contentHeight" }
        rowHeights[rowIndex] = contentHeight
      }
    }

    if (canonicalStyle?.borderLeft == true &&
        (columnIndex > 0 || tableStyle?.borderStyle != BorderStyle.Hidden)) {
      debug {
        val oldValue = if (columnBorderWidths[columnIndex] == 0) "0 ->" else "already"
        "  ($rowIndex, $columnIndex) Left border $oldValue 1"
      }
      columnBorderWidths[columnIndex] = 1
    }
    if (canonicalStyle?.borderRight == true &&
        (columnIndex + columnSpan < columnCount - 1 || tableStyle?.borderStyle != BorderStyle.Hidden)) {
      debug {
        val oldValue = if (columnBorderWidths[columnIndex + columnSpan] == 0) "0 ->" else "already"
        "  ($rowIndex, $columnIndex) Right border $oldValue 1"
      }
      columnBorderWidths[columnIndex + columnSpan] = 1
    }
    if (canonicalStyle?.borderTop == true &&
        (rowIndex > 0 || tableStyle?.borderStyle != BorderStyle.Hidden)) {
      debug {
        val oldValue = if (rowBorderHeights[rowIndex] == 0) "0 ->" else "already"
        "  ($rowIndex, $columnIndex) Top border $oldValue 1"
      }
      rowBorderHeights[rowIndex] = 1
    }
    if (canonicalStyle?.borderBottom == true &&
        (rowIndex + rowSpan < rowCount - 1 || tableStyle?.borderStyle != BorderStyle.Hidden)) {
      debug {
        val oldValue = if (rowBorderHeights[rowIndex + rowSpan] == 0) "0 ->" else "already"
        "  ($rowIndex, $columnIndex) Bottom border $oldValue 1"
      }
      rowBorderHeights[rowIndex + rowSpan] = 1
    }
  }
  debug {
    """
    | Intermediate row heights: ${rowHeights.contentToString()}
    | Intermediate row border heights: ${rowBorderHeights.contentToString()}
    | Intermediate column widths: ${columnWidths.contentToString()}
    | Intermediate column border widths: ${columnBorderWidths.contentToString()}
    """.trimMargin()
  }

  debug { " 2/2..." }

  positionedCells.filter { it.cell.columnSpan > 1 }
      .sortedBy { it.cell.columnSpan }
      .forEach { (rowIndex, columnIndex, cell) ->
        val layout = layouts.getValue(cell)
        val columnSpan = cell.columnSpan
        val contentWidth = layout.measureWidth()
        val columnSpanIndices = columnIndex until columnIndex + columnSpan
        val currentSpanColumnWidth = columnSpanIndices.sumBy { columnWidths[it] }
        val currentSpanBorderWidth = (columnIndex + 1 until columnIndex + columnSpan).sumBy { columnBorderWidths[it] }
        val currentSpanWidth = currentSpanColumnWidth + currentSpanBorderWidth
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
            debug { "  ($rowIndex, $columnIndex) Increasing column $targetColumnIndex width from $currentWidth to $newWidth" }
            columnWidths[targetColumnIndex] = newWidth
          }
        }
      }

  positionedCells.filter { it.cell.rowSpan > 1 }
      .sortedBy { it.cell.rowSpan }
      .forEach { (rowIndex, columnIndex, cell) ->
        val layout = layouts.getValue(cell)
        val rowSpan = cell.rowSpan
        val contentHeight = layout.measureHeight()
        val rowSpanIndices = rowIndex until rowIndex + rowSpan
        val currentSpanRowHeight = rowSpanIndices.sumBy { rowHeights[it] }
        val currentSpanBorderHeight = (rowIndex + 1 until rowIndex + rowSpan).sumBy { rowBorderHeights[it] }
        val currentSpanHeight = currentSpanRowHeight + currentSpanBorderHeight
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
            debug { "  ($rowIndex, $columnIndex) Increasing row $targetRowIndex height from $currentHeight to $newHeight" }
            rowHeights[targetRowIndex] = newHeight
          }
        }
      }
  debug {
    """
    | Final row heights: ${rowHeights.contentToString()}
    | Final row border heights: ${rowBorderHeights.contentToString()}
    | Final column widths: ${columnWidths.contentToString()}
    | Final column border widths: ${columnBorderWidths.contentToString()}
    """.trimMargin()
  }

  debug { "Layout pass..." }

  val tableLefts = IntArray(columnWidths.size + 1)
  val tableWidth: Int
  run {
    var left = 0
    for (i in columnWidths.indices) {
      tableLefts[i] = left
      left += columnWidths[i] + columnBorderWidths[i]
    }
    tableLefts[columnWidths.size] = left
    tableWidth = left + columnBorderWidths[columnWidths.size]
  }

  val tableTops = IntArray(rowHeights.size + 1)
  val tableHeight: Int
  run {
    var top = 0
    for (i in rowHeights.indices) {
      tableTops[i] = top
      top += rowHeights[i] + rowBorderHeights[i]
    }
    tableTops[rowHeights.size] = top
    tableHeight = top + rowBorderHeights[rowHeights.size]
  }
  debug {
    """
    | Width: $tableWidth
    | Height: $tableHeight
    | Lefts: ${tableLefts.contentToString()}
    | Tops: ${tableTops.contentToString()}
    """.trimMargin()
  }

  debug { "Drawing pass..." }

  val surface = TextSurface(tableWidth, tableHeight)

  debug { " Borders..." }
  for (rowIndex in 0..rowCount) {
    val rowDrawStartIndex = tableTops[rowIndex]

    for (columnIndex in 0..columnCount) {
      val positionedCell = getOrNull(rowIndex, columnIndex)
      val cell = positionedCell?.cell
      val cellCanonicalStyle = positionedCell?.canonicalStyle

      val previousRowPositionedCell = getOrNull(rowIndex, columnIndex - 1)
      val previousRowCell = previousRowPositionedCell?.cell
      val previousRowCellCanonicalStyle = previousRowPositionedCell?.canonicalStyle

      val previousColumnPositionedCell = getOrNull(rowIndex - 1, columnIndex)
      val previousColumnCell = previousColumnPositionedCell?.cell
      val previousColumnCellCanonicalStyle = previousColumnPositionedCell?.canonicalStyle

      val columnDrawStartIndex = tableLefts[columnIndex]
      val rowBorderHeight = rowBorderHeights[rowIndex]
      val hasRowBorder = rowBorderHeight != 0
      val columnBorderWidth = columnBorderWidths[columnIndex]
      val hasColumnBorder = columnBorderWidth != 0
      if (hasRowBorder && hasColumnBorder) {
        val previousRowColumnPositionedCell = getOrNull(rowIndex - 1, columnIndex - 1)
        val previousRowColumnCell = previousRowColumnPositionedCell?.cell
        val previousRowColumnCellCanonicalStyle = previousRowColumnPositionedCell?.canonicalStyle

        val cornerTopBorder = previousRowColumnCell !== previousColumnCell &&
            (previousRowColumnCellCanonicalStyle?.borderRight == true ||
                previousColumnCellCanonicalStyle?.borderLeft == true)
        val cornerLeftBorder = previousRowColumnCell !== previousRowCell &&
            (previousRowColumnCellCanonicalStyle?.borderBottom == true ||
                previousRowCellCanonicalStyle?.borderTop == true)
        val cornerBottomBorder = previousRowCell !== cell &&
            (previousRowCellCanonicalStyle?.borderRight == true ||
                cellCanonicalStyle?.borderLeft == true)
        val cornerRightBorder = previousColumnCell !== cell &&
            (previousColumnCellCanonicalStyle?.borderBottom == true ||
                cellCanonicalStyle?.borderTop == true)
        if (cornerTopBorder || cornerLeftBorder || cornerBottomBorder || cornerRightBorder) {
          val borderChar = border.get(
              down = cornerBottomBorder,
              up = cornerTopBorder,
              left = cornerLeftBorder,
              right = cornerRightBorder)
          debug { "  ($rowIndex, $columnIndex) corner '$borderChar': ($rowDrawStartIndex, $columnDrawStartIndex)" }
          surface[rowDrawStartIndex, columnDrawStartIndex] = borderChar
        }
      }

      if (hasColumnBorder &&
          previousRowCell !== cell &&
          (previousRowCellCanonicalStyle?.borderRight == true ||
              cellCanonicalStyle?.borderLeft == true)) {
        val rowDrawEndIndex = tableTops[rowIndex + 1] // Safe given cell != null.
        val borderChar = border.vertical
        debug { "  ($rowIndex, $columnIndex) left '$borderChar': (${rowDrawStartIndex + 1}, $columnDrawStartIndex) -> ($rowDrawEndIndex, $columnDrawStartIndex)" }
        for (rowDrawIndex in rowDrawStartIndex + rowBorderHeight until rowDrawEndIndex) {
          surface[rowDrawIndex, columnDrawStartIndex] = borderChar
        }
      }

      if (hasRowBorder &&
          previousColumnCell !== cell &&
          (previousColumnCellCanonicalStyle?.borderBottom == true ||
              cellCanonicalStyle?.borderTop == true)) {
        val columnDrawEndIndex = tableLefts[columnIndex + 1] // Safe given cell != null
        val borderChar = border.horizontal
        debug { "  ($rowIndex, $columnIndex) top '$borderChar': ($rowDrawStartIndex, ${columnDrawStartIndex + 1}) -> ($rowDrawStartIndex, $columnDrawEndIndex)" }
        for (columnDrawIndex in columnDrawStartIndex + columnBorderWidth until columnDrawEndIndex) {
          surface[rowDrawStartIndex, columnDrawIndex] = borderChar
        }
      }
    }
  }

  debug { " Cells..." }
  positionedCells.forEach { (rowIndex, columnIndex, cell) ->
    val cellLeft = tableLefts[columnIndex] + columnBorderWidths[columnIndex]
    val cellRight = tableLefts[columnIndex + cell.columnSpan]
    val cellTop = tableTops[rowIndex] + rowBorderHeights[rowIndex]
    val cellBottom = tableTops[rowIndex + cell.rowSpan]

    debug {
      """
      |  ($rowIndex, $columnIndex) clip:
      |    horizontal [$cellLeft, $cellRight)
      |    vertical [$cellTop, $cellBottom)
      """.trimMargin()
    }

    val canvas = surface.clip(cellLeft, cellRight, cellTop, cellBottom)
    val layout = layouts.getValue(cell)
    layout.draw(canvas)
  }

  return surface.toString()
}

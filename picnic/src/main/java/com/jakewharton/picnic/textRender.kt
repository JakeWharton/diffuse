@file:JvmName("TextRendering")

package com.jakewharton.picnic

private val String.width: Int get() {
  return split('\n').map { it.length }.max() ?: 0
}

private val String.height: Int get() = 1 + count { it == '\n' }

private fun String.renderLine(line: Int, width: Int): String {
  return split('\n').getOrElse(line) { "" }.padEnd(width)
}

@Suppress("NOTHING_TO_INLINE", "UNUSED_PARAMETER")
private inline fun debugln(string: String) {
  //println(string)
}

@JvmName("render")
fun Table.renderText(): String {
  val rows = header.rows + body.rows + footer.rows

  val columnWidths = IntCounts()
  val rowHeights = IntCounts()
  val rowSpanCarries = IntCounts()

  debugln("First pass...")
  rows.forEachIndexed { rowIndex, row ->
    debugln(" Row $rowIndex:")

    var columnIndex = 0
    row.cells.forEach { cell ->
      // Check for any previous rows' cells whose >1 rowSpan carries them into this row.
      // When found, advance the column index to avoid them, pushing remaining cells to the right.
      while (columnIndex < rowSpanCarries.size && rowSpanCarries[columnIndex] > 0) {
        debugln("  Found carry for column $columnIndex")
        rowSpanCarries[columnIndex]--
        columnIndex++
      }

      val columnSpan = cell.columnSpan
      if (columnSpan == 1) {
        val currentWidth = columnWidths[columnIndex]
        val contentWidth = cell.content.width
        if (contentWidth > currentWidth) {
          debugln("  Increasing column $columnIndex width from $currentWidth to $contentWidth")
          columnWidths[columnIndex] = contentWidth
        }
      }

      val rowSpan = cell.rowSpan
      if (rowSpan == 1) {
        val currentHeight = rowHeights[rowIndex]
        val contentHeight = cell.content.height
        if (contentHeight > currentHeight) {
          debugln("  Increasing row $rowIndex height from $currentHeight to $contentHeight")
          rowHeights[rowIndex] = contentHeight
        }
      }

      val rowSpanCarry = rowSpan - 1
      repeat(columnSpan) {
        rowSpanCarries[columnIndex] = rowSpanCarry
        columnIndex++
      }
    }

    debugln("  Row span carry: $rowSpanCarries")
  }

  debugln("Second pass...")
  rowSpanCarries.clear()
  rows.forEachIndexed { rowIndex, row ->
    debugln(" Row $rowIndex:")

    var columnIndex = 0
    row.cells.forEach { cell ->
      // Check for any previous rows' cells whose >1 rowSpan carries them into this row.
      // When found, advance the column index to avoid them, pushing remaining cells to the right.
      while (columnIndex < rowSpanCarries.size && rowSpanCarries[columnIndex] > 0) {
        debugln("  Found carry for column $columnIndex")
        rowSpanCarries[columnIndex]--
        columnIndex++
      }

      // TODO start with size 2 and work upward. This allows required distribution of extra size
      //  for smaller spans to be re-used in larger spans (potentially avoiding expansion).

      val columnSpan = cell.columnSpan
      if (columnSpan > 1) {
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
            debugln("Increasing column $targetColumnIndex width from $currentWidth to $newWidth")
            columnWidths[targetColumnIndex] = newWidth
          }
        }
      }

      val rowSpan = cell.rowSpan
      if (rowSpan > 1) {
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
            debugln("Increasing row $targetRowIndex height from $currentHeight to $newHeight")
            rowHeights[targetRowIndex] = newHeight
          }
        }
      }

      val rowSpanCarry = rowSpan - 1
      repeat(columnSpan) {
        rowSpanCarries[columnIndex] = rowSpanCarry
        columnIndex++
      }
    }

    debugln("  Row span carry: $rowSpanCarries")
  }

  debugln("Done")
  debugln(" Column widths: $columnWidths")
  debugln(" Row heights: $rowHeights")

  rowSpanCarries.clear()
  return buildString {
    rows.forEachIndexed { rowIndex, row ->
      for (rowLine in 0 until rowHeights[rowIndex]) {
        var columnIndex = 0
        row.cells.forEach { cell ->
          // TODO this is super broken because these cells should render in this space!
          // Check for any previous rows' cells whose >1 rowSpan carries them into this row.
          // When found, print empty space for the column's width.
          while (columnIndex < rowSpanCarries.size && rowSpanCarries[columnIndex] > 0) {
            rowSpanCarries[columnIndex]--
            repeat(columnWidths[columnIndex]) {
              append(' ')
            }
            columnIndex++
          }

          val columnSpan = cell.columnSpan
          val cellWidth = when (columnSpan) {
            1 -> columnWidths[columnIndex]
            else -> (columnIndex until columnIndex + columnSpan).sumBy { columnWidths[it] }
          }
          append(cell.content.renderLine(rowLine, cellWidth))


          val rowSpanCarry = cell.rowSpan - 1
          repeat(columnSpan) {
            rowSpanCarries[columnIndex] = rowSpanCarry
            columnIndex++
          }
        }
        appendln()
      }
    }
  }
}

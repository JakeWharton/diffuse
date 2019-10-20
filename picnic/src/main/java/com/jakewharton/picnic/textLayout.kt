package com.jakewharton.picnic

import com.jakewharton.picnic.Table.PositionedCell
import com.jakewharton.picnic.TextAlignment.BottomCenter
import com.jakewharton.picnic.TextAlignment.BottomLeft
import com.jakewharton.picnic.TextAlignment.BottomRight
import com.jakewharton.picnic.TextAlignment.MiddleCenter
import com.jakewharton.picnic.TextAlignment.MiddleLeft
import com.jakewharton.picnic.TextAlignment.MiddleRight
import com.jakewharton.picnic.TextAlignment.TopCenter
import com.jakewharton.picnic.TextAlignment.TopLeft
import com.jakewharton.picnic.TextAlignment.TopRight

interface TextLayout {
  fun measureWidth(): Int
  fun measureHeight(): Int

  fun draw(canvas: TextCanvas)
}

internal class SimpleLayout(private val cell: PositionedCell) : TextLayout {
  private val leftPadding = cell.canonicalStyle?.paddingLeft ?: 0
  private val topPadding = cell.canonicalStyle?.paddingTop ?: 0

  override fun measureWidth(): Int {
    return leftPadding +
        (cell.canonicalStyle?.paddingRight ?: 0) +
        (cell.cell.content.split('\n').map { it.length }.max() ?: 0)
  }

  override fun measureHeight(): Int {
    return 1 +
        topPadding +
        (cell.canonicalStyle?.paddingBottom ?: 0) +
        cell.cell.content.count { it == '\n' }
  }

  override fun draw(canvas: TextCanvas) {
    val width = measureWidth()
    val height = measureHeight()

    val alignment = cell.canonicalStyle?.alignment ?: TopLeft
    val left = when (alignment) {
      TopLeft, MiddleLeft, BottomLeft -> leftPadding
      TopCenter, MiddleCenter, BottomCenter -> ((canvas.width - width) / 2) + leftPadding
      TopRight, MiddleRight, BottomRight -> canvas.width - width + leftPadding
    }
    val top = when (alignment) {
      TopLeft, TopCenter, TopRight -> topPadding
      MiddleLeft, MiddleCenter, MiddleRight -> ((canvas.height - height) / 2) + topPadding
      BottomLeft, BottomCenter, BottomRight -> canvas.height - height + topPadding
    }

    var x = left
    var y = top
    for (char in cell.cell.content) {
      // TODO invisible chars, codepoints, graphemes, etc.
      if (char != '\n') {
        canvas[y, x++] = char
      } else {
        y++
        x = left
      }
    }
  }
}

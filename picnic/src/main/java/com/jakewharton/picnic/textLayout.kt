package com.jakewharton.picnic

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

internal class SimpleLayout(private val cell: Cell) : TextLayout {
  override fun measureWidth(): Int {
    return cell.paddingLeft +
        cell.paddingRight +
        (cell.content.split('\n').map { it.length }.max() ?: 0)
  }

  override fun measureHeight(): Int {
    return 1 +
        cell.paddingTop +
        cell.paddingBottom +
        cell.content.count { it == '\n' }
  }

  override fun draw(canvas: TextCanvas) {
    val width = measureWidth()
    val height = measureHeight()

    val left = when (cell.alignment) {
      TopLeft, MiddleLeft, BottomLeft -> cell.paddingLeft
      TopCenter, MiddleCenter, BottomCenter -> (canvas.width - width) / 2
      TopRight, MiddleRight, BottomRight -> canvas.width - width + cell.paddingLeft
    }
    val top = when (cell.alignment) {
      TopLeft, TopCenter, TopRight -> cell.paddingTop
      MiddleLeft, MiddleCenter, MiddleRight -> (canvas.height - height) / 2
      BottomLeft, BottomCenter, BottomRight -> canvas.height - height + cell.paddingTop
    }

    var x = left
    var y = top
    for (char in cell.content) {
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

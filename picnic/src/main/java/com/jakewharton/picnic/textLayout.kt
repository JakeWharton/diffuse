package com.jakewharton.picnic

interface TextLayout {
  fun measureWidth(): Int
  fun measureHeight(): Int

  fun draw(canvas: TextCanvas)
}

internal class SimpleLayout(private val cell: Cell) : TextLayout {
  override fun measureWidth(): Int {
    return cell.content.split('\n').map { it.length }.max() ?: 0
  }

  override fun measureHeight(): Int {
    return 1 + cell.content.count { it == '\n' }
  }

  override fun draw(canvas: TextCanvas) {
    var x = 0
    var y = 0
    for (char in cell.content) {
      // TODO invisible chars, codepoints, graphemes, etc.
      if (char != '\n') {
        canvas[y, x++] = char
      } else {
        y++
        x = 0
      }
    }
  }
}

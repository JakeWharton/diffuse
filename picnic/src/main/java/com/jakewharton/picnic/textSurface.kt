package com.jakewharton.picnic

internal class TextSurface(
  override val width: Int,
  override val height: Int
) : TextCanvas {
  private val buffer = StringBuilder(height * (width + 1)).apply {
    repeat(height) {
      repeat(width) {
        append(' ')
      }
      append('\n')
    }
  }

  override operator fun set(row: Int, column: Int, char: Char) {
    require(row in 0 until height) { "Row $row not in range [0, $height)" }
    require(column in 0 until width) { "Column $column not in range [0, $width)" }
    buffer[row * (width + 1) + column] = char
  }

  override fun get(row: Int, column: Int): Char {
    require(row in 0 until height) { "Row $row not in range [0, $height)" }
    require(column in 0 until width) { "Column $column not in range [0, $width)" }
    return buffer[row * (width + 1) + column]
  }

  override fun toString() = buffer.toString()
}

interface TextCanvas {
  val width: Int
  val height: Int

  operator fun set(row: Int, column: Int, char: Char)
  operator fun get(row: Int, column: Int): Char

  fun clip(left: Int, right: Int, top: Int, bottom: Int): TextCanvas {
    return ClippedTextCanvas(this, left, right, top, bottom)
  }
}

private class ClippedTextCanvas(
  private val canvas: TextCanvas,
  private val left: Int,
  right: Int,
  private val top: Int,
  bottom: Int
) : TextCanvas {
  override val width = right - left
  override val height = bottom - top

  override fun set(row: Int, column: Int, char: Char) {
    require(row in 0 until height) { "Row $row not in range [0, $height)" }
    require(column in 0 until width) { "Column $column not in range [0, $width)" }
    canvas[top + row, left + column] = char
  }

  override fun get(row: Int, column: Int): Char {
    require(row in 0 until height) { "Row $row not in range [0, $height)" }
    require(column in 0 until width) { "Column $column not in range [0, $width)" }
    return canvas[top + row, left + column]
  }
}

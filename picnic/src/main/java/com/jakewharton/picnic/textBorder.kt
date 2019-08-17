package com.jakewharton.picnic

class TextBorder(private val value: String) {
  init {
    require(value.length == 16) { "Border string must contain exactly 16 characters" }
  }

  val empty get() = value[0]
  val down get() = value[1]
  val up get() = value[2]
  val vertical get() = value[3]
  val right get() = value[4]
  val downAndRight get() = value[5]
  val upAndRight get() = value[6]
  val verticalAndRight get() = value[7]
  val left get() = value[8]
  val downAndLeft get() = value[9]
  val upAndLeft get() = value[10]
  val verticalAndLeft get() = value[11]
  val horizontal get() = value[12]
  val downAndHorizontal get() = value[13]
  val upAndHorizontal get() = value[14]
  val verticalAndHorizontal get() = value[15]

  fun get(
    down: Boolean = false,
    up: Boolean = false,
    right: Boolean = false,
    left: Boolean = false
  ): Char {
    return value[
        (if (down) 1 else 0) or
        (if (up) 2 else 0) or
        (if (right) 4 else 0) or
        (if (left) 8 else 0)
    ]
  }

  companion object {
    @JvmField val DEFAULT = TextBorder(" ╷╵│╶┌└├╴┐┘┤─┬┴┼")
    @JvmField val ASCII = TextBorder("   | +++ +++-+++")
  }
}

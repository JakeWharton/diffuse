package com.jakewharton.diffuse

import com.jakewharton.byteunits.BinaryByteUnit

inline class Size(val bytes: Long) {
  override fun toString(): String = if (bytes >= 0) {
    BinaryByteUnit.format(bytes)
  } else {
    "-" + BinaryByteUnit.format(-bytes)
  }

  operator fun plus(other: Size) = Size(bytes + other.bytes)
  operator fun minus(other: Size) = Size(bytes - other.bytes)

  companion object {
    val ZERO = Size(0)
  }
}

package com.jakewharton.diffuse.io

import com.jakewharton.byteunits.BinaryByteUnit
import kotlin.math.absoluteValue

@JvmInline
value class Size(val bytes: Long) : Comparable<Size> {
  override fun toString(): String = if (bytes >= 0) {
    BinaryByteUnit.format(bytes)
  } else {
    "-" + BinaryByteUnit.format(-bytes)
  }

  override fun compareTo(other: Size) = bytes.compareTo(other.bytes)

  operator fun plus(other: Size) = Size(bytes + other.bytes)
  operator fun minus(other: Size) = Size(bytes - other.bytes)
  operator fun unaryMinus() = Size(-bytes)

  val absoluteValue get() = Size(bytes.absoluteValue)

  companion object {
    val ZERO = Size(0)
  }
}

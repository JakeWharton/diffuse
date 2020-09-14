package com.jakewharton.diffuse.io

import com.jakewharton.byteunits.BinaryByteUnit
import java.text.DecimalFormat
import java.text.NumberFormat
import kotlin.math.abs
import kotlin.math.absoluteValue

// Move ByteSize and formatPair to BinaryByteUnit ?
data class ByteSize(val quantity: String, val unit: String)

/**
 * Return `bytes` as human-readable ByteSize (e.g., {"1.2", "GiB"}.
 * This will use `format` for formatting the number.
 */
fun formatPair(bytes: Long, format: NumberFormat): ByteSize {
  val UNITS_CLONE = arrayOf("B", "KiB", "MiB", "GiB", "TiB", "PiB")
  val absolutebytes = abs(bytes)
  var unitIndex = 0
  var count = bytes.toDouble()
  while (count >= 1024.0 && unitIndex < UNITS_CLONE.size - 1) {
    count /= 1024.0
    unitIndex += 1
  }

  if (bytes >= 0) {
    return ByteSize(format.format(count), UNITS_CLONE.get(unitIndex))
  } else {
    return ByteSize("-" + format.format(count), UNITS_CLONE.get(unitIndex))
  }
}

inline class Size(val bytes: Long) : Comparable<Size> {
  override fun toString(): String = if (bytes >= 0) {
    BinaryByteUnit.format(bytes)
  } else {
    "-" + BinaryByteUnit.format(-bytes)
  }

  fun toStringPair(): ByteSize {
    return formatPair(bytes, DecimalFormat("#,##0.#"))
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

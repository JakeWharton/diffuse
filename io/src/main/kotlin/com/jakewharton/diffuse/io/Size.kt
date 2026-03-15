package com.jakewharton.diffuse.io

import kotlin.math.absoluteValue
import me.saket.bytesize.binaryBytes
import me.saket.bytesize.decimalBytes

enum class ByteUnit {
  Binary,
  Decimal,
}

@JvmInline
value class Size(val bytes: Long) : Comparable<Size> {
  override fun toString(): String = toString(ByteUnit.Binary)

  fun toString(format: ByteUnit = ByteUnit.Binary): String =
    when (format) {
      ByteUnit.Binary -> bytes.binaryBytes
      ByteUnit.Decimal -> bytes.decimalBytes
    }.toString()

  override fun compareTo(other: Size) = bytes.compareTo(other.bytes)

  operator fun plus(other: Size) = Size(bytes + other.bytes)

  operator fun minus(other: Size) = Size(bytes - other.bytes)

  operator fun unaryMinus() = Size(-bytes)

  val absoluteValue
    get() = Size(bytes.absoluteValue)

  companion object {
    val ZERO = Size(0)
  }
}

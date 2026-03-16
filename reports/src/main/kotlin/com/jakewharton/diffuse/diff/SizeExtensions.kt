package com.jakewharton.diffuse.diff

import kotlin.math.absoluteValue
import me.saket.bytesize.BinaryByteSize
import me.saket.bytesize.ByteSize
import me.saket.bytesize.DecimalBitSize
import me.saket.bytesize.DecimalByteSize

/** TODO: https://github.com/saket/byte-size/pull/21 */
internal val ByteSize.absoluteValue: ByteSize
  get() =
    when (this) {
      is DecimalBitSize -> DecimalBitSize(inWholeBits.absoluteValue)
      is BinaryByteSize -> BinaryByteSize(inWholeBytes.absoluteValue)
      is DecimalByteSize -> DecimalByteSize(inWholeBytes.absoluteValue)
    }

/** TODO: https://github.com/saket/byte-size/pull/20 */
@Suppress("NOTHING_TO_INLINE")
internal inline operator fun ByteSize.unaryMinus(): ByteSize =
  when (this) {
    is DecimalBitSize -> DecimalBitSize(-inWholeBits)
    is BinaryByteSize -> BinaryByteSize(-inWholeBytes)
    is DecimalByteSize -> DecimalByteSize(-inWholeBytes)
  }

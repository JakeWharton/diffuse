package com.jakewharton.diffuse.diff

import kotlin.math.absoluteValue
import me.saket.bytesize.BinaryByteSize
import me.saket.bytesize.ByteSize
import me.saket.bytesize.DecimalBitSize
import me.saket.bytesize.DecimalByteSize

/** TODO: https://github.com/saket/byte-size/issues/22 */
internal val ByteSize.absoluteValue: ByteSize
  get() =
    when (this) {
      is DecimalBitSize -> DecimalBitSize(inWholeBits.absoluteValue)
      is BinaryByteSize -> BinaryByteSize(inWholeBytes.absoluteValue)
      is DecimalByteSize -> DecimalByteSize(inWholeBytes.absoluteValue)
    }

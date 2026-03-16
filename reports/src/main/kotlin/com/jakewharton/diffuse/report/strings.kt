package com.jakewharton.diffuse.report

import me.saket.bytesize.ByteSize

internal fun Int.toUnitString(unit: String, vararg specializations: Pair<Int, String>): String {
  return buildString {
    append(this@toUnitString)
    append(' ')
    append(specializations.toMap()[this@toUnitString] ?: unit)
  }
}

internal fun Int.toDiffString(zeroSign: Char? = null) = buildString {
  if (this@toDiffString > 0) {
    append('+')
  } else if (this@toDiffString == 0 && zeroSign != null) {
    append(zeroSign)
  }
  append(this@toDiffString)
}

internal fun ByteSize.toDiffString() = buildString {
  if (inWholeBytes > 0L) {
    append('+')
  }
  append(this@toDiffString)
}

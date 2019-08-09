package com.jakewharton.diffuse

internal fun Int.toUnitString(unit: String, vararg specializations: Pair<Int, String>): String {
  return buildString {
    append(this@toUnitString)
    append(' ')
    append(specializations.toMap()[this@toUnitString] ?: unit)
  }
}

internal fun Int.toDiffString() = buildString {
  if (this@toDiffString > 0) {
    append('+')
  }
  append(this@toDiffString)
}

internal fun Size.toDiffString() = buildString {
  if (bytes > 0L) {
    append('+')
  }
  append(this@toDiffString)
}

package com.jakewharton.dex

internal fun <T> List<T>.defaultIfEmpty(value: T): List<T> {
  return if (isNotEmpty()) this else listOf(value)
}

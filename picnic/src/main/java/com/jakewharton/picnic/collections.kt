package com.jakewharton.picnic

internal class IntCounts(capacity: Int = 10) {
  private var data = IntArray(capacity)

  var size = 0
    private set

  operator fun get(index: Int) = if (index >= size) 0 else data[index]

  operator fun set(index: Int, value: Int) {
    val newSize = index + 1
    if (newSize > data.size) {
      data = data.copyOf(data.size * 2)
    }
    data[index] = value
    size = size.coerceAtLeast(newSize)
  }

  override fun toString() = buildString {
    append('[')
    repeat(size) {
      if (it > 0) {
        append(", ")
      }
      append(data[it])
    }
    append(']')
  }

  fun clear() {
    data = IntArray(10)
    size = 0
  }
}

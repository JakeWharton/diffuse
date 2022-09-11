package com.jakewharton.diffuse.diff.lint

internal data class Notice(
  val message: String,
  val type: Type,
) : Comparable<Notice> {
  override fun compareTo(other: Notice) = comparator.compare(this, other)

  enum class Type {
    // Note: Order determines sorting of Notice instances!
    Resolution,
    Warning,
    Informational,
  }

  private companion object {
    val comparator = compareBy(Notice::type, Notice::message)
  }
}

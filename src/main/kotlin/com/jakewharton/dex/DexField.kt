package com.jakewharton.dex

import kotlin.comparisons.compareBy
import kotlin.comparisons.thenBy

/** Represents a single field reference. */
data class DexField(
    val declaringType: String,
    val name: String,
    val type: String
) : Comparable<DexField> {
  override fun toString(): String {
    val returnName = type.substringAfterLast('.')
    return "$declaringType $name: $returnName"
  }

  override fun compareTo(other: DexField) = COMPARATOR.compare(this, other)

  companion object {
    private val COMPARATOR = compareBy(DexField::declaringType, DexField::name)
        .thenBy(DexField::type)
  }
}

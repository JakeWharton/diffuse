package com.jakewharton.dex

import kotlin.comparisons.compareBy
import kotlin.comparisons.thenBy

/** Represents a single method reference. */
data class DexMethod(
    val declaringType: String,
    val name: String,
    val parameterTypes: List<String>,
    val returnType: String
) : Comparable<DexMethod> {
  fun render(hideSyntheticNumbers: Boolean = false): String {
    val method = if (hideSyntheticNumbers && name.matches(SYNTHETIC_SUFFIX)) {
      name.substring(0, name.lastIndexOf('$'))
    } else name
    val parameters = parameterTypes.joinToString(", ") { it.substringAfterLast('.') }

    if (returnType == "void") {
      return "$declaringType $method($parameters)"
    }
    val returnName = returnType.substringAfterLast('.')
    return "$declaringType $method($parameters) → $returnName"
  }

  override fun toString() = render()

  override fun compareTo(other: DexMethod) = COMPARATOR.compare(this, other)

  companion object {
    private val SYNTHETIC_SUFFIX = ".*?\\$\\d+".toRegex()
    private val COMPARATOR = compareBy(DexMethod::declaringType, DexMethod::name)
        .thenBy(comparingValues(), DexMethod::parameterTypes)
        .thenBy(DexMethod::returnType)

    // TODO replace with https://youtrack.jetbrains.com/issue/KT-20690
    private fun <T : Comparable<T>> comparingValues(): Comparator<Iterable<T>> {
      return object : Comparator<Iterable<T>> {
        override fun compare(o1: Iterable<T>, o2: Iterable<T>): Int {
          val i1 = o1.iterator()
          val i2 = o2.iterator()
          while (true) {
            if (!i1.hasNext()) {
              return if (!i2.hasNext()) 0 else -1
            }
            if (!i2.hasNext()) {
              return 1
            }
            val result = i1.next().compareTo(i2.next())
            if (result != 0) {
              return result
            }
          }
        }
      }
    }
  }
}

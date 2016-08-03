package com.jakewharton.dex

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
    val parameters = parameterTypes.map { typeOnly(it) }.joinToString(", ")

    if (returnType == "void") {
      return "$declaringType $method($parameters)"
    }
    val returnName = typeOnly(returnType)
    return "$declaringType $method($parameters) → $returnName"
  }

  override fun toString() = render()

  override fun compareTo(other: DexMethod): Int {
    declaringType.compareTo(other.declaringType).let { if (it != 0) return it }
    name.compareTo(other.name).let { if (it != 0) return it }
    parameterTypes.size.compareTo(other.parameterTypes.size).let { if (it != 0) return it }
    parameterTypes.zip(other.parameterTypes).forEach {
      it.component1().compareTo(it.component2()).let { if (it != 0) return it }
    }
    returnType.compareTo(other.returnType).let { if (it != 0) return it }
    return 0
  }

  private fun typeOnly(type: String): String {
    val lastDot = type.lastIndexOf('.')
    return if (lastDot == -1) type else type.substring(lastDot + 1)
  }

  companion object {
    private val SYNTHETIC_SUFFIX = ".*?\\$\\d+".toRegex()
  }
}

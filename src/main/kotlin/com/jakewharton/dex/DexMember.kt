package com.jakewharton.dex

sealed class DexMember : Comparable<DexMember> {
  abstract val declaringType: String
  abstract val name: String

  open fun render(hideSyntheticNumbers: Boolean = false) = toString()

  override fun compareTo(other: DexMember): Int {
    val typeResult = declaringType.compareTo(other.declaringType)
    if (typeResult != 0) {
      return typeResult
    }
    if (javaClass == other.javaClass) {
      return 0
    }
    return if (this is DexMethod) -1 else 1
  }
}

/** Represents a single field reference. */
data class DexField(
  override val declaringType: String,
  override val name: String,
  val type: String
) : DexMember() {
  override fun toString(): String {
    val returnName = type.substringAfterLast('.')
    return "$declaringType $name: $returnName"
  }

  override fun compareTo(other: DexMember): Int {
    val superResult = super.compareTo(other)
    if (superResult != 0) {
      return superResult
    }
    return COMPARATOR.compare(this, other as DexField)
  }

  private companion object {
    val COMPARATOR = compareBy(DexField::name, DexField::type)
  }
}

/** Represents a single method reference. */
data class DexMethod(
  override val declaringType: String,
  override val name: String,
  val parameterTypes: List<String>,
  val returnType: String
) : DexMember() {
  override fun render(hideSyntheticNumbers: Boolean): String {
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

  override fun compareTo(other: DexMember): Int {
    val superResult = super.compareTo(other)
    if (superResult != 0) {
      return superResult
    }
    return COMPARATOR.compare(this, other as DexMethod)
  }

  private companion object {
    val SYNTHETIC_SUFFIX = ".*?\\$\\d+".toRegex()
    val COMPARATOR = compareBy(DexMethod::name)
        .thenBy(comparingValues(), DexMethod::parameterTypes)
        .thenBy(DexMethod::returnType)
  }
}

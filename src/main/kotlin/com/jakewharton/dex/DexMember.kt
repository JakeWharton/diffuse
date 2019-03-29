package com.jakewharton.dex

sealed class DexMember : Comparable<DexMember> {
  abstract val declaringType: String
  abstract val name: String
  abstract fun render(hideSyntheticNumbers: Boolean = false): String

  final override fun toString() = render()

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
  override fun render(hideSyntheticNumbers: Boolean): String {
    var displayType = declaringType
    if (hideSyntheticNumbers && displayType.matches(LAMBDA_CLASS_SUFFIX)) {
      displayType = displayType.substringBeforeLast('$')
    }
    val returnName = type.substringAfterLast('.')
    return "$displayType $name: $returnName"
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
    var displayType = declaringType
    if (hideSyntheticNumbers && displayType.matches(LAMBDA_CLASS_SUFFIX)) {
      displayType = displayType.substringBeforeLast('$')
    }

    var displayName = name
    if (hideSyntheticNumbers) {
      if (name.startsWith("lambda$")) {
        LAMBDA_METHOD_NUMBER.find(name)?.let { match ->
          displayName = name.removeRange(match.range.start, match.range.endInclusive)
        }
      } else if (name.matches(SYNTHETIC_METHOD_SUFFIX)) {
        displayName = displayName.substring(0, name.lastIndexOf('$'))
      }
    }

    val parameters = parameterTypes.joinToString(", ") { it.substringAfterLast('.') }

    if (returnType == "void") {
      return "$displayType $displayName($parameters)"
    }
    val returnName = returnType.substringAfterLast('.')
    return "$displayType $displayName($parameters) → $returnName"
  }

  override fun compareTo(other: DexMember): Int {
    val superResult = super.compareTo(other)
    if (superResult != 0) {
      return superResult
    }
    return COMPARATOR.compare(this, other as DexMethod)
  }

  private companion object {
    val SYNTHETIC_METHOD_SUFFIX = ".*?\\$\\d+".toRegex()
    val LAMBDA_METHOD_NUMBER = "\\$\\d+\\$".toRegex()
    val COMPARATOR = compareBy(DexMethod::name)
        .thenBy(comparingValues(), DexMethod::parameterTypes)
        .thenBy(DexMethod::returnType)
  }
}

private val LAMBDA_CLASS_SUFFIX = ".*?\\$\\\$Lambda\\$\\d+".toRegex()

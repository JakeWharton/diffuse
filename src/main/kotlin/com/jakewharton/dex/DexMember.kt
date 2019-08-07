package com.jakewharton.dex

inline class Descriptor(private val value: String) : Comparable<Descriptor> {
  override fun compareTo(other: Descriptor): Int {
    return sourceName.compareTo(other.sourceName)
  }

  val sourceName get() = value.toHumanName()
  val simpleName get() = sourceName.substringAfterLast('.')

  val arrayArity get() = value.indexOfFirst { it != '[' }
  val componentDescriptor get() = Descriptor(value.substring(arrayArity))
  fun asArray(arity: Int = 1) = Descriptor("[".repeat(arity) + value)

  fun withoutLambdaSuffix(): Descriptor {
    return when (value.matches(LAMBDA_CLASS_SUFFIX)) {
      true -> Descriptor(value.substringBeforeLast('$') + ";")
      false -> this
    }
  }

  companion object {
    val VOID = Descriptor("V")
    private val LAMBDA_CLASS_SUFFIX = ".*?\\$\\\$Lambda\\$\\d+;".toRegex()

    private fun String.toHumanName(): String {
      if (startsWith("[")) {
        return substring(1).toHumanName() + "[]"
      }
      if (startsWith("L")) {
        return substring(1, length - 1).replace('/', '.')
      }
      return when (this) {
        "B" -> "byte"
        "C" -> "char"
        "D" -> "double"
        "F" -> "float"
        "I" -> "int"
        "J" -> "long"
        "S" -> "short"
        "V" -> "void"
        "Z" -> "boolean"
        else -> throw IllegalArgumentException("Unknown type $this")
      }
    }
  }
}

sealed class DexMember : Comparable<DexMember> {
  abstract val declaringType: Descriptor
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
  override val declaringType: Descriptor,
  override val name: String,
  val type: Descriptor
) : DexMember() {
  override fun render(hideSyntheticNumbers: Boolean): String {
    var displayType = declaringType
    if (hideSyntheticNumbers) {
      displayType = displayType.withoutLambdaSuffix()
    }
    return "${displayType.sourceName} $name: ${type.simpleName}"
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
  override val declaringType: Descriptor,
  override val name: String,
  val parameterTypes: List<Descriptor>,
  val returnType: Descriptor
) : DexMember() {
  override fun render(hideSyntheticNumbers: Boolean): String {
    var displayType = declaringType
    if (hideSyntheticNumbers) {
      displayType = displayType.withoutLambdaSuffix()
    }

    var displayName = name
    if (hideSyntheticNumbers) {
      if (name.startsWith("lambda$")) {
        LAMBDA_METHOD_NUMBER.find(name)?.let { match ->
          displayName = name.removeRange(match.range.first, match.range.last)
        }
      } else if (name.matches(SYNTHETIC_METHOD_SUFFIX)) {
        displayName = displayName.substring(0, name.lastIndexOf('$'))
      }
    }

    return buildString {
      append(displayType.sourceName)
      append(' ')
      append(displayName)
      append('(')
      parameterTypes.joinTo(this, ", ", transform = Descriptor::simpleName)
      append(')')
      if (returnType != Descriptor.VOID) {
        append(" → ")
        append(returnType.simpleName)
      }
    }
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

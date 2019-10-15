package com.jakewharton.dex

sealed class DexMember : Comparable<DexMember> {
  abstract val declaringType: TypeDescriptor
  abstract val name: String

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
  override val declaringType: TypeDescriptor,
  override val name: String,
  val type: TypeDescriptor
) : DexMember() {
  override fun toString() = "${declaringType.sourceName} $name: ${type.simpleName}"

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
  override val declaringType: TypeDescriptor,
  override val name: String,
  val parameterTypes: List<TypeDescriptor>,
  val returnType: TypeDescriptor
) : DexMember() {
  override fun toString() = buildString {
    append(declaringType.sourceName)
    append(' ')
    append(name)
    append('(')
    parameterTypes.joinTo(this, ", ", transform = TypeDescriptor::simpleName)
    append(')')
    if (returnType != VOID) {
      append(" → ")
      append(returnType.simpleName)
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
    val VOID = TypeDescriptor("V")
    val COMPARATOR = compareBy(DexMethod::name)
        .thenBy(comparingValues(), DexMethod::parameterTypes)
        .thenBy(DexMethod::returnType)
  }
}

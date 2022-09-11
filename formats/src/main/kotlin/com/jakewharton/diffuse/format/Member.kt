package com.jakewharton.diffuse.format

sealed class Member : Comparable<Member> {
  abstract val declaringType: TypeDescriptor
  abstract val name: String

  override fun compareTo(other: Member): Int {
    val typeResult = declaringType.compareTo(other.declaringType)
    if (typeResult != 0) {
      return typeResult
    }
    if (javaClass == other.javaClass) {
      return 0
    }
    return if (this is Method) -1 else 1
  }
}

data class Field(
  override val declaringType: TypeDescriptor,
  override val name: String,
  val type: TypeDescriptor,
) : Member() {
  override fun toString() = "${declaringType.sourceName} $name: ${type.simpleName}"

  override fun compareTo(other: Member): Int {
    val superResult = super.compareTo(other)
    if (superResult != 0) {
      return superResult
    }
    return COMPARATOR.compare(this, other as Field)
  }

  private companion object {
    val COMPARATOR = compareBy(Field::name, Field::type)
  }
}

data class Method(
  override val declaringType: TypeDescriptor,
  override val name: String,
  val parameterTypes: List<TypeDescriptor>,
  val returnType: TypeDescriptor,
) : Member() {
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

  override fun compareTo(other: Member): Int {
    val superResult = super.compareTo(other)
    if (superResult != 0) {
      return superResult
    }
    return COMPARATOR.compare(this, other as Method)
  }

  private companion object {
    val VOID = TypeDescriptor("V")
    val COMPARATOR = compareBy(Method::name)
      .thenBy(comparingValues(), Method::parameterTypes)
      .thenBy(Method::returnType)

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

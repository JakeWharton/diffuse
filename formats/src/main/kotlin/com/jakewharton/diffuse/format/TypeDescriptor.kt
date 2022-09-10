package com.jakewharton.diffuse.format

@JvmInline
value class TypeDescriptor(val value: String) : Comparable<TypeDescriptor> {
  override fun compareTo(other: TypeDescriptor): Int {
    return sourceName.compareTo(other.sourceName)
  }

  val sourceName get() = value.toHumanName()
  val simpleName get() = sourceName.substringAfterLast('.')

  val arrayArity get() = value.indexOfFirst { it != '[' }
  val componentDescriptor get() = TypeDescriptor(value.substring(arrayArity))
  fun asArray(arity: Int = 1) = TypeDescriptor("[".repeat(arity) + value)

  override fun toString() = sourceName

  companion object {
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

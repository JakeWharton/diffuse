package com.jakewharton.diffuse

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
  val type: TypeDescriptor
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
  val returnType: TypeDescriptor
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
  }
}

internal fun Member.withoutSyntheticSuffix() = when (this) {
  is Field -> withoutSyntheticSuffix()
  is Method -> withoutSyntheticSuffix()
}

private fun Field.withoutSyntheticSuffix(): Field {
  val newDeclaredType = declaringType.withoutSyntheticSuffix()
  if (newDeclaredType == declaringType) {
    return this
  }
  return copy(declaringType = newDeclaredType)
}

private val syntheticMethodSuffix = ".*?\\$\\d+".toRegex()
private val lambdaMethodNumber = "\\$\\d+\\$".toRegex()

private fun Method.withoutSyntheticSuffix(): Method {
  val newDeclaredType = declaringType.withoutSyntheticSuffix()
  val lambdaName = name.startsWith("lambda$")
  val syntheticName = name.matches(syntheticMethodSuffix)

  if (declaringType == newDeclaredType && !lambdaName && !syntheticName) {
    return this
  }

  val newName = when {
    lambdaName -> lambdaMethodNumber.find(name)!!.let { match ->
      name.removeRange(match.range.first, match.range.last)
    }
    syntheticName -> name.substring(0, name.lastIndexOf('$'))
    else -> name
  }
  return copy(declaringType = newDeclaredType, name = newName)
}

private val lambdaClassSuffix = ".*?\\$\\\$Lambda\\$\\d+;".toRegex()

private fun TypeDescriptor.withoutSyntheticSuffix(): TypeDescriptor {
  return when (value.matches(lambdaClassSuffix)) {
    true -> TypeDescriptor(value.substringBeforeLast('$') + ";")
    false -> this
  }
}

package com.jakewharton.diffuse

import com.jakewharton.diffuse.format.Field
import com.jakewharton.diffuse.format.Member
import com.jakewharton.diffuse.format.Method
import com.jakewharton.diffuse.format.TypeDescriptor

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
private val lambdaMethodNumber = "\\$\\d+(\\$|$)".toRegex()

private fun Method.withoutSyntheticSuffix(): Method {
  val newDeclaredType = declaringType.withoutSyntheticSuffix()
  val lambdaName = name.startsWith("lambda$")
  val syntheticName = name.matches(syntheticMethodSuffix)

  if (declaringType == newDeclaredType && !lambdaName && !syntheticName) {
    return this
  }

  val newName = when {
    lambdaName -> lambdaMethodNumber.find(name)?.let { match ->
      val endIndex = if (match.range.last == name.lastIndex) name.length else match.range.last
      name.removeRange(match.range.first, endIndex)
    } ?: name
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

package com.jakewharton.dex

import java.util.*

/** Data class for outputting and comparing method outputs. */
data class MethodName(
        val name: String, val className: TypeName, val returnType: TypeName,
        val params: Array<TypeName>) {

  override fun equals(other : Any?) : Boolean = when(other) {
    is MethodName -> toString().equals(other.toString())
    else -> false
  }

  override fun hashCode() = toString().hashCode()

  override fun toString() : String {
    val paramString = params.map { it }.joinToString(", ")
    val prefix = "$className $name($paramString)"
    return when {
      returnType.primitive && returnType.name == "void" -> prefix
      else -> "$prefix → $returnType"
    }
  }

}

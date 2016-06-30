package com.jakewharton.dex

/** Simple representation of a type name. Marks primitives to prevent deobfuscation attempts. */
data class TypeName(val name: String, val primitive: Boolean = false, val array: Boolean = false) {

  companion object {
    fun parse(type: String, stripPackage: Boolean = false) : TypeName = when {
      type.startsWith("[") -> parse(type.substring(1), stripPackage).toArrayType()
      type.startsWith("L") -> {
        val name = type.substring(1, type.length - 1)
        if (stripPackage) TypeName(name.split('/').last())
        else TypeName(name.replace('/', '.'))
      }
      else -> when(type) {
        "B" -> TypeName("byte", primitive = true)
        "C" -> TypeName("char", primitive = true)
        "D" -> TypeName("double", primitive = true)
        "F" -> TypeName("float", primitive = true)
        "I" -> TypeName("int", primitive = true)
        "J" -> TypeName("long", primitive = true)
        "S" -> TypeName("short", primitive = true)
        "V" -> TypeName("void", primitive = true)
        "Z" -> TypeName("boolean", primitive = true)
        else -> throw IllegalArgumentException("Unknown type $type")
      }
    }
  }

  override fun toString() : String = "$name" + if (array) "[]" else ""

  fun toArrayType() = TypeName(name, primitive=primitive, array=true)
}


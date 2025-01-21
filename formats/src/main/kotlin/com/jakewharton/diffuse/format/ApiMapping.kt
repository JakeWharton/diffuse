package com.jakewharton.diffuse.format

import com.jakewharton.diffuse.io.Input

class ApiMapping private constructor(private val typeMappings: Map<TypeDescriptor, TypeMapping>) {
  override fun equals(other: Any?) = other is ApiMapping && typeMappings == other.typeMappings
  override fun hashCode() = typeMappings.hashCode()
  override fun toString() = typeMappings.toString()

  fun isEmpty() = typeMappings.isEmpty()

  val types get() = typeMappings.size
  val methods get() = typeMappings.values.sumOf { it.methods.size }
  val fields get() = typeMappings.values.sumOf { it.fields.size }

  /**
   * Given a [TypeDescriptor] which is typically obfuscated, return a new [TypeDescriptor] for the
   * original name or return [type] if not included in the mapping.
   */
  operator fun get(type: TypeDescriptor): TypeDescriptor {
    return typeMappings[type.componentDescriptor]
      ?.typeDescriptor
      ?.asArray(type.arrayArity)
      ?: type
  }

  /**
   * Given a [Member] which is typically obfuscated, return a new [Member] with the types and
   * name mapped back to their original values or return [member] if the declaring type is not
   * included in the mapping.
   */
  operator fun get(member: Member) = when (member) {
    is Field -> this[member]
    is Method -> this[member]
  }

  /**
   * Given a [Field] which is typically obfuscated, return a new [Field] with the types and
   * name mapped back to their original values or return [field] if the declaring type is not
   * included in the mapping.
   */
  operator fun get(field: Field): Field {
    val declaringType = field.declaringType.componentDescriptor
    val declaringTypeMapping = typeMappings[declaringType] ?: return field

    val newDeclaringType = declaringTypeMapping.typeDescriptor
      .asArray(field.declaringType.arrayArity)
    val newType = this[field.type]
    val newName = declaringTypeMapping[field.name] ?: field.name
    return Field(newDeclaringType, newName, newType)
  }

  /**
   * Given a [Method] which is typically obfuscated, return a new [Method] with the types and
   * name mapped back to their original values or return [method] if the declaring type is not
   * included in the mapping.
   */
  operator fun get(method: Method): Method {
    val declaringType = method.declaringType.componentDescriptor
    val declaringTypeMapping = typeMappings[declaringType] ?: return method

    val newDeclaringType = declaringTypeMapping.typeDescriptor
      .asArray(method.declaringType.arrayArity)
    val newReturnType = this[method.returnType]
    val newParameters = method.parameterTypes.map(::get)
    val signature = MethodSignature(newReturnType, method.name, newParameters)
    val newName = declaringTypeMapping[signature] ?: method.name
    return Method(newDeclaringType, newName, newParameters, newReturnType)
  }

  companion object {
    @JvmField
    val EMPTY = ApiMapping(emptyMap())

    @JvmStatic
    @JvmName("parse")
    fun Input.toApiMapping(): ApiMapping {
      val typeMappings = mutableMapOf<TypeDescriptor, TypeMapping>()

      var fromDescriptor: TypeDescriptor? = null
      var toDescriptor: TypeDescriptor? = null
      var fields: MutableMap<String, String>? = null
      var methods: MutableMap<MethodSignature, String>? = null
      source().use { bufferedSource ->
        generateSequence { bufferedSource.readUtf8Line() }.forEachIndexed { index, line ->
          if (line.trimStart().startsWith('#') || line.isBlank()) {
            return@forEachIndexed
          }
          if (line.startsWith(' ')) {
            val result = memberLine.matchEntire(line)
              ?: throw IllegalArgumentException(
                "Unable to parse line ${index + 1} as member mapping: $line",
              )
            val (_, returnType, fromName, parameters, toName) = result.groupValues

            if (parameters != "") {
              val returnDescriptor = humanNameToDescriptor(returnType)
              val parameterDescriptors = parameters
                .substring(1, parameters.lastIndex) // Remove leading '(' and trailing ')'.
                .takeUnless(String::isEmpty) // Do not process parameter-less methods.
                ?.split(',')
                ?.map(Companion::humanNameToDescriptor)
                ?: emptyList()

              val lookupSignature = MethodSignature(returnDescriptor, toName, parameterDescriptors)
              methods!![lookupSignature] = fromName
            } else {
              fields!![toName] = fromName
            }
          } else {
            if (fromDescriptor != null) {
              typeMappings[toDescriptor!!] = TypeMapping(fromDescriptor!!, fields!!, methods!!)
            }

            val result = typeLine.matchEntire(line)
              ?: throw IllegalArgumentException(
                "Unable to parse line ${index + 1} as type mapping: $line",
              )
            val (_, fromType, toType) = result.groupValues

            fromDescriptor = humanNameToDescriptor(fromType)
            toDescriptor = humanNameToDescriptor(toType)
            fields = mutableMapOf()
            methods = mutableMapOf()
          }
        }
      }
      if (fromDescriptor != null) {
        typeMappings[toDescriptor!!] = TypeMapping(fromDescriptor!!, fields!!, methods!!)
      }
      return ApiMapping(typeMappings)
    }

    private val typeLine = Regex("(.+?) -> (.+?):")
    private val memberLine = Regex("\\s+(?:\\d+:\\d+:)?(.+?) (.+?)(\\(.*?\\))?(?::\\d+:\\d+)? -> (.+)")

    private fun humanNameToDescriptor(name: String): TypeDescriptor {
      val type = name.trimEnd('[', ']')
      val descriptor = when (type) {
        "void" -> "V"
        "boolean" -> "Z"
        "byte" -> "B"
        "char" -> "C"
        "double" -> "D"
        "float" -> "F"
        "int" -> "I"
        "long" -> "J"
        "short" -> "S"
        else -> "L${type.replace('.', '/')};"
      }
      val arrayArity = (name.length - type.length) / 2
      return TypeDescriptor(descriptor).asArray(arrayArity)
    }
  }
}

private data class MethodSignature(
  val returnType: TypeDescriptor,
  val name: String,
  val parameterTypes: List<TypeDescriptor>,
)

private data class TypeMapping(
  val typeDescriptor: TypeDescriptor,
  val fields: Map<String, String>,
  val methods: Map<MethodSignature, String>,
) {
  operator fun get(field: String) = fields[field]
  operator fun get(method: MethodSignature) = methods[method]
}

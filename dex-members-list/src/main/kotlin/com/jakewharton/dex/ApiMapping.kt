package com.jakewharton.dex

import java.io.File
import java.nio.charset.Charset
import java.nio.file.Path

class ApiMapping private constructor(private val typeMappings: Map<TypeDescriptor, TypeMapping>) {
  override fun equals(other: Any?) = other is ApiMapping && typeMappings == other.typeMappings
  override fun hashCode() = typeMappings.hashCode()
  override fun toString() = typeMappings.toString()

  val types get() = typeMappings.size
  val methods get() = typeMappings.values.sumBy { it.methods.size }
  val fields get() = typeMappings.values.sumBy { it.fields.size }

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
   * Given a [DexMember] which is typically obfuscated, return a new [DexMember] with the types and
   * name mapped back to their original values or return [member] if the declaring type is not
   * included in the mapping.
   */
  operator fun get(member: DexMember) = when (member) {
    is DexField -> this[member]
    is DexMethod -> this[member]
  }

  /**
   * Given a [DexField] which is typically obfuscated, return a new [DexField] with the types and
   * name mapped back to their original values or return [field] if the declaring type is not
   * included in the mapping.
   */
  operator fun get(field: DexField): DexField {
    val declaringType = field.declaringType.componentDescriptor
    val declaringTypeMapping = typeMappings[declaringType] ?: return field

    val newDeclaringType = declaringTypeMapping.typeDescriptor
        .asArray(field.declaringType.arrayArity)
    val newType = this[field.type]
    val newName = declaringTypeMapping[field.name] ?: field.name
    return DexField(newDeclaringType, newName, newType)
  }

  /**
   * Given a [DexMethod] which is typically obfuscated, return a new [DexMethod] with the types and
   * name mapped back to their original values or return [method] if the declaring type is not
   * included in the mapping.
   */
  operator fun get(method: DexMethod): DexMethod {
    val declaringType = method.declaringType.componentDescriptor
    val declaringTypeMapping = typeMappings[declaringType] ?: return method

    val newDeclaringType = declaringTypeMapping.typeDescriptor
        .asArray(method.declaringType.arrayArity)
    val newReturnType = this[method.returnType]
    val newParameters = method.parameterTypes.map(::get)
    val signature = MethodSignature(newReturnType, method.name, newParameters)
    val newName = declaringTypeMapping[signature] ?: method.name
    return DexMethod(newDeclaringType, newName, newParameters, newReturnType)
  }

  companion object {
    @JvmField
    val EMPTY = ApiMapping(emptyMap())

    @JvmStatic
    @JvmOverloads
    @JvmName("fromPath")
    fun Path.toApiMapping(charset: Charset = Charsets.UTF_8): ApiMapping {
      return readBytes().toString(charset).toApiMapping()
    }

    @JvmStatic
    @JvmOverloads
    @JvmName("fromFile")
    fun File.toApiMapping(charset: Charset = Charsets.UTF_8): ApiMapping {
      return readText(charset).toApiMapping()
    }

    @JvmStatic
    @JvmName("fromString")
    fun String.toApiMapping(): ApiMapping {
      val typeMappings = mutableMapOf<TypeDescriptor, TypeMapping>()

      var fromDescriptor: TypeDescriptor? = null
      var toDescriptor: TypeDescriptor? = null
      var fields: MutableMap<String, String>? = null
      var methods: MutableMap<MethodSignature, String>? = null
      split('\n').forEachIndexed { index, line ->
        if (line.startsWith('#') || line.isBlank()) {
          return@forEachIndexed
        }
        if (line.startsWith(' ')) {
          val result = memberLine.matchEntire(line)
              ?: throw IllegalArgumentException(
                  "Unable to parse line ${index + 1} as member mapping: $line")
          val (_, returnType, fromName, parameters, toName) = result.groupValues

          if (parameters != "") {
            val returnDescriptor = humanNameToDescriptor(returnType)
            val parameterDescriptors = parameters
                .substring(1, parameters.lastIndex) // Remove leading '(' and trailing ')'.
                .takeUnless(String::isEmpty) // Do not process parameter-less methods.
                ?.split(',')
                ?.map(::humanNameToDescriptor)
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
                  "Unable to parse line ${index + 1} as type mapping: $line")
          val (_, fromType, toType) = result.groupValues

          fromDescriptor = humanNameToDescriptor(fromType)
          toDescriptor = humanNameToDescriptor(toType)
          fields = mutableMapOf()
          methods = mutableMapOf()
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
  val parameterTypes: List<TypeDescriptor>
)

private data class TypeMapping(
  val typeDescriptor: TypeDescriptor,
  val fields: Map<String, String>,
  val methods: Map<MethodSignature, String>
) {
  operator fun get(field: String) = fields[field]
  operator fun get(method: MethodSignature) = methods[method]
}

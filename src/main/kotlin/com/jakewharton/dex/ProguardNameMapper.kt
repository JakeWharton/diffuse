package com.jakewharton.dex

import java.io.File
import java.util.*

/** Maps proguard-obfuscated symbol names back to their original name. */
class ProguardNameMapper(mappingFile: String) : (MethodName) -> MethodName {
  data class ClassMapping(
          val original: String, val obfuscated: String,
          val methods: MutableList<MethodMapping> = ArrayList())
  data class MethodMapping(
      val original: String, val obfuscated: String, val returnType: String,
      val params: List<String>)

  // Matches class renames in Proguard mappings.
  val classRegex = Regex("""^(\S+) -> (\S+):$""")
  // Matches nested method renames in Proguard mappings.
  val methodRegex = Regex("""^    (\d+:\d+:)?(\S+) (\S+)\((\S*)\)(:\d+)? -> (\S+)$""")

  val typeMappings : Map<String, String>
  val reverseTypeMappings : Map<String, String>
  val methods : Map<MethodName, MethodName>

  init {
    val mappingList = File(mappingFile).useLines {
      it.fold<String, MutableList<ClassMapping>>(
          ArrayList<ClassMapping>(), { mappings, line -> mappings + line })
    }

    typeMappings = mappingList.associateBy({ it.original }, { it.obfuscated })
    methods = mappingList.flatMap { cls ->
      cls.methods.map {
        val originalParams = it.params.map { parseProguardType(it) }
        val obfuscatedParams = it.params.map { parseAndMapProguardType(it) }
        val originalReturnType = parseProguardType(it.returnType)
        val obfuscatedReturnType = parseAndMapProguardType(it.returnType)
        Pair(
            MethodName(
                it.obfuscated, parseProguardType(cls.obfuscated, stripPackage = false),
                obfuscatedReturnType, obfuscatedParams.toTypedArray()),
            MethodName(
                it.original, parseProguardType(cls.original, stripPackage=false), originalReturnType,
                originalParams.toTypedArray())
        )
      }

    }.associateBy({ it.first }, { it.second })
    reverseTypeMappings = typeMappings.entries.associateBy({ it.value }, { it.key })
  }

  fun <T> MatchResult?.valuesOnMatch(fn : (List<String>) -> T) : T?
       = if (this != null) fn(this.groupValues) else null

  operator fun MutableList<ClassMapping>.plus(mapping: ClassMapping) : MutableList<ClassMapping> {
    add(mapping)
    return this
  }

  operator fun MutableList<ClassMapping>.plus(mapping: MethodMapping)
          : MutableList<ClassMapping> {
    last().methods.add(mapping)
    return this
  }

  operator fun MutableList<ClassMapping>.plus(line: String) : MutableList<ClassMapping> =
      classRegex.matchEntire(line).valuesOnMatch {
          this + ClassMapping(it[1], it[2])
      } ?: methodRegex.matchEntire(line).valuesOnMatch {
          this + MethodMapping(it[3], it[6], it[2], it[4].split(','))
      } ?: this

  private fun String.stripPackage() : String = this.split(".").last()

  private fun parseProguardType(type: String, stripPackage: Boolean=true) : TypeName = when {
    type.endsWith("[]") -> parseProguardType(type.substring(0, type.length - 2)).toArrayType()
    else -> TypeName(
        if (stripPackage) type.stripPackage() else type,
        primitive = type in arrayOf(
            "byte", "char", "double", "float", "int", "long", "short", "void", "boolean"))
  }

  private fun parseAndMapProguardType(type: String) : TypeName =
      mapType(parseProguardType(typeMappings.get(type) ?: type))

  private fun mapType(type: TypeName) = when {
    type.primitive -> type
    else -> type.copy(name=typeMappings.get(type.name) ?: type.name)
  }

  private fun reverseMapType(type: TypeName) = when {
    type.primitive -> type
    else -> type.copy(name=reverseTypeMappings.get(type.name) ?: type.name)
  }

    override fun invoke(name: MethodName) : MethodName {
      val mapped = methods.get(name)
      return when (mapped) {
        null -> name.copy(
            className = reverseMapType(name.className),
            returnType = reverseMapType(name.returnType),
            params = name.params.map { reverseMapType(it) }.toTypedArray())
        else -> mapped
      }
    }

}

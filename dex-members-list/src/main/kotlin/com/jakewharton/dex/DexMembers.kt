@file:JvmName("DexMembers")

package com.jakewharton.dex

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.switch
import com.github.ajalt.clikt.parameters.options.validate
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.path
import com.jakewharton.dex.DexParser.Companion.toDexParser
import com.jakewharton.dex.DexParser.Desugaring
import com.jakewharton.diffuse.ApiMapping
import com.jakewharton.diffuse.ApiMapping.Companion.toApiMapping
import com.jakewharton.diffuse.Field
import com.jakewharton.diffuse.Member
import com.jakewharton.diffuse.Method
import com.jakewharton.diffuse.TypeDescriptor
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

fun main(vararg args: String) {
  MembersCommand().main(args.toList())
}

private class MembersCommand : CliktCommand(name = "dex-members-list") {
  private val hideSyntheticNumbers by option("--hide-synthetic-numbers",
      help = "Remove synthetic numbers from type and method names. This is useful to prevent noise when diffing output.")
      .flag()

  private val mapping by option("--mapping",
      help = "Obfuscation mapping file produced by R8 or ProGuard for de-obfuscating names.")
      .path(exists = true, folderOkay = false, readable = true)

  private val desugaring by option("--desugar",
      help = "Enable desugaring of language features and newer API calls.\n\nConfigure with --min-api and --library.")
      .flag()

  private val minApiLevel by option("--min-api", metavar = "API",
      help = "The minimum API level supported. This affects how much desugaring will occur.")
      .int()
      .default(Desugaring.DISABLED.minApiLevel)
      .validate { require(it > 0) }

  private val libraryJars by option("--library", metavar = "JAR",
      help = "Specify a library .jar used for desugaring. Typically this is an android.jar from the Android SDK or rt.jar from the JDK.")
      .path(exists = true, folderOkay = false, readable = true)
      .multiple()

  private val inputs: List<File> by argument(name = "FILES",
      help = ".apk, .aar, .jar, .dex, and/or .class files to process. STDIN is used when no files are provided.")
      .convert { File(it) }
      .multiple(true)

  private val mode by option(help = "Limit to only methods or fields")
      .switch("--methods" to Mode.Methods, "--fields" to Mode.Fields)
      .default(Mode.Members)

  enum class Mode {
    Methods, Fields, Members;
  }

  override fun run() {
    val inputs = inputs.map(::FileInputStream)
        .ifEmpty { listOf(System.`in`) }
        .map { it.use(InputStream::readBytes) }
    val parser = inputs.toDexParser()
        .withApiMapping(mapping?.toApiMapping() ?: ApiMapping.EMPTY)
        .apply {
          if (desugaring) {
            withDesugaring(Desugaring(minApiLevel, libraryJars))
          }
        }
    val list = when (mode) {
      Mode.Members -> parser.listMembers()
      Mode.Methods -> parser.listMethods()
      Mode.Fields -> parser.listFields()
    }
    val displayList = if (hideSyntheticNumbers) {
      list.map { it.withoutSyntheticSuffix() }
    } else {
      list
    }
    // Re-sort because rendering may subtly change ordering.
    displayList.sorted().forEach(::println)
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

private val SYNTHETIC_METHOD_SUFFIX = ".*?\\$\\d+".toRegex()
private val LAMBDA_METHOD_NUMBER = "\\$\\d+\\$".toRegex()

private fun Method.withoutSyntheticSuffix(): Method {
  val newDeclaredType = declaringType.withoutSyntheticSuffix()
  val lambdaName = name.startsWith("lambda$")
  val syntheticName = name.matches(SYNTHETIC_METHOD_SUFFIX)

  if (declaringType == newDeclaredType && !lambdaName && !syntheticName) {
    return this
  }

  val newName = when {
    lambdaName -> LAMBDA_METHOD_NUMBER.find(name)!!.let { match ->
      name.removeRange(match.range.first, match.range.last)
    }
    syntheticName -> name.substring(0, name.lastIndexOf('$'))
    else -> name
  }
  return copy(declaringType = newDeclaredType, name = newName)
}

private val LAMBDA_CLASS_SUFFIX = ".*?\\$\\\$Lambda\\$\\d+;".toRegex()

private fun TypeDescriptor.withoutSyntheticSuffix(): TypeDescriptor {
  return when (value.matches(LAMBDA_CLASS_SUFFIX)) {
    true -> TypeDescriptor(value.substringBeforeLast('$') + ";")
    false -> this
  }
}

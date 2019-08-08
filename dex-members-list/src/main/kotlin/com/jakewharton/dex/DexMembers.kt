@file:JvmName("DexMembers")

package com.jakewharton.dex

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.switch
import com.jakewharton.dex.ApiMapping.Companion.toApiMapping
import com.jakewharton.dex.DexParser.Companion.toDexParser
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

  private val mapping: ApiMapping by option("--mapping",
      help = "Obfuscation mapping file produced by R8 or ProGuard for de-obfuscating names.")
      .convert { File(it).toApiMapping() }
      .default(ApiMapping.EMPTY)

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
        .withApiMapping(mapping)
    val list = when (mode) {
      Mode.Members -> parser.list()
      Mode.Methods -> parser.listMethods()
      Mode.Fields -> parser.listFields()
    }
    list.map { it.render(hideSyntheticNumbers) }
        .sorted() // Re-sort because rendering may subtly change ordering.
        .forEach(::println)
  }
}

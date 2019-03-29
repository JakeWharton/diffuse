package com.jakewharton.dex

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.switch
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

internal abstract class BaseCommand(name: String) : CliktCommand(name = name) {
  private val legacyDx: Boolean by option("--legacy-dx",
      help = "Use legacy 'dx' dex compiler instead of D8").flag()

  private val hideSyntheticNumbers by option("--hide-synthetic-numbers",
      help = "Remove synthetic numbers from type and method names. This is useful to prevent noise when diffing output.")
      .flag()

  private val inputs: List<File> by argument(name = "FILES",
      help = ".apk, .aar, .jar, .dex, and/or .class files to process. STDIN is used when no files are provided.")
      .convert { File(it) }
      .multiple(true)

  enum class Mode {
    Methods, Fields, Members;
  }

  fun print(mode: Mode) {
    val inputs = inputs.map(::FileInputStream)
        .ifEmpty { listOf(System.`in`) }
        .map { it.use(InputStream::readBytes) }
    val parser = DexParser.fromBytes(inputs).withLegacyDx(legacyDx)
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

internal class MembersCommand : BaseCommand("dex-members-list") {
  private val mode by option(help = "Limit to only methods or fields")
      .switch("--methods" to Mode.Methods, "--fields" to Mode.Fields)
      .default(Mode.Members)

  override fun run() = print(mode)
}

internal class FieldCommand : BaseCommand("dex-field-list") {
  override fun run() = print(Mode.Fields)
}

internal class MethodCommand : BaseCommand("dex-method-list") {
  override fun run() = print(Mode.Methods)
}

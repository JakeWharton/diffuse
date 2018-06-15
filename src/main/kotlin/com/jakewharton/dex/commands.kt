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

internal abstract class BaseCommand(name: String) : CliktCommand(name = name) {
  val legacyDx: Boolean by option("--legacy-dx",
      help = "Use legacy 'dx' dex compiler instead of D8").flag()

  private val inputs: List<File> by argument(name = "FILES",
      help = ".apk, .aar, .jar, .dex, and/or .class files to process. STDIN is used when no files are provided.")
      .convert { File(it) }
      .multiple(true)

  fun loadInputs() = inputs
      .map(::FileInputStream)
      .defaultIfEmpty(System.`in`)
      .map { it.use { it.readBytes() } }
      .toList()
}

internal class MembersCommand : BaseCommand("dex-members-list") {
  private val hideSyntheticNumbers by option("--hide-synthetic-numbers",
      help = "Remove number suffixes from synthetic accessor methods. This is useful to prevent noise when diffing output.")
      .flag()

  private val mode by option(help = "Limit to only methods or fields")
      .switch("--methods" to Mode.Methods, "--fields" to Mode.Fields)
      .default(Mode.Members)

  sealed class Mode {
    object Methods : Mode()
    object Fields : Mode()
    object Members : Mode()
  }

  override fun run() {
    val parser = DexParser.fromBytes(loadInputs()).withLegacyDx(legacyDx)
    val list = when (mode) {
      Mode.Members -> parser.list()
      Mode.Methods -> parser.listMethods()
      Mode.Fields -> parser.listFields()
    }
    if (hideSyntheticNumbers && mode == Mode.Fields) {
      println("WARN: --hide-synthetic-numbers has no effect when --fields is used.")
    }
    list.map { it.render(hideSyntheticNumbers) }.forEach(::println)
  }
}

internal class FieldCommand : BaseCommand("dex-field-list") {
  override fun run() {
    DexParser.fromBytes(loadInputs())
        .withLegacyDx(legacyDx)
        .listFields()
        .forEach(::println)
  }
}

internal class MethodCommand : BaseCommand("dex-method-list") {
  private val hideSyntheticNumbers by option("--hide-synthetic-numbers",
      help = "Remove number suffixes from synthetic accessor methods. This is useful to prevent noise when diffing output.")
      .flag()

  override fun run() {
    DexParser.fromBytes(loadInputs())
        .withLegacyDx(legacyDx)
        .listMethods()
        .map { it.render(hideSyntheticNumbers) }
        .forEach(::println)
  }
}

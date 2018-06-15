package com.jakewharton.dex

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import java.io.File
import java.io.FileInputStream

internal abstract class BaseCommand(name: String) : CliktCommand(name = name) {
  val legacyDx: Boolean by option("--legacy-dx",
      help = "Use legacy 'dx' dex compiler instead of D8").flag()

  val inputs: List<File> by argument(name = "FILES",
      help = ".apk, .aar, .jar, .dex, and/or .class files to process. STDIN is used when no files are provided.")
      .convert { File(it) }
      .multiple(true)

  fun loadInputs(inputs: List<File>) = inputs
      .map(::FileInputStream)
      .defaultIfEmpty(System.`in`)
      .map { it.use { it.readBytes() } }
      .toList()
}

internal class FieldCommand : BaseCommand("dex-field-list") {
  override fun run() {
    DexParser.fromBytes(loadInputs(inputs))
        .withLegacyDx(legacyDx)
        .listFields()
        .forEach(::println)
  }
}

internal class MethodCommand : BaseCommand("dex-method-list") {
  val hideSyntheticNumbers by option("--hide-synthetic-numbers",
      help = "Remove number suffixes from synthetic accessor methods. This is useful to prevent noise when diffing output.")
      .flag()

  override fun run() {
    DexParser.fromBytes(loadInputs(inputs))
        .withLegacyDx(legacyDx)
        .listMethods()
        .map { it.render(hideSyntheticNumbers) }
        .forEach(::println)
  }
}

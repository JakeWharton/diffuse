@file:JvmName("Diffuse")

package com.jakewharton.diffuse

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.NoRunCliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.defaultLazy
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.switch
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.path
import com.jakewharton.diffuse.Aab.Companion.toAab
import com.jakewharton.diffuse.Aab.Module
import com.jakewharton.diffuse.Aar.Companion.toAar
import com.jakewharton.diffuse.ApiMapping.Companion.toApiMapping
import com.jakewharton.diffuse.Apk.Companion.toApk
import com.jakewharton.diffuse.Jar.Companion.toJar
import com.jakewharton.diffuse.diff.BinaryDiff
import com.jakewharton.diffuse.io.Input
import com.jakewharton.diffuse.io.Input.Companion.asInput
import java.io.PrintStream
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import kotlin.LazyThreadSafetyMode.NONE

fun main(vararg args: String) {
  val defaultFs = FileSystems.getDefault()
  val systemOut = System.out

  NoRunCliktCommand(name = "diffuse")
      .subcommands(
          DiffCommand(defaultFs, defaultFs, systemOut),
          MembersCommand(defaultFs, systemOut))
      .main(args.toList())
}

private enum class Type {
  Apk, Aar, Aab, Jar
}

private class DiffCommand(
  inputFs: FileSystem,
  outputFs: FileSystem,
  output: PrintStream
) : CliktCommand(name = "diff") {
  private val inputOptions by object : OptionGroup("Input options") {
    private val type by option(help = "File type of OLD and NEW. Default is 'apk'.")
        .switch("--apk" to Type.Apk, "--aar" to Type.Aar, "--aab" to Type.Aab, "--jar" to Type.Jar)
        .default(Type.Apk)

    private val oldMappingPath by option(
          "--old-mapping",
          help = "Mapping file produced by R8 or ProGuard.",
          metavar = "FILE"
        )
        .path(exists = true, folderOkay = false, readable = true)

    private val newMappingPath by option(
          "--new-mapping",
          help = "Mapping file produced by R8 or ProGuard.",
          metavar = "FILE"
        )
        .path(exists = true, folderOkay = false, readable = true)

    fun parse(old: Input, new: Input): BinaryDiff {
      val oldMapping = oldMappingPath?.asInput()?.toApiMapping() ?: ApiMapping.EMPTY
      val newMapping = newMappingPath?.asInput()?.toApiMapping() ?: ApiMapping.EMPTY
      return when (type) {
        Type.Apk -> BinaryDiff.ofApk(old.toApk(), oldMapping, new.toApk(), newMapping)
        Type.Aab -> BinaryDiff.ofAab(old.toAab(), new.toAab())
        Type.Aar -> BinaryDiff.ofAar(old.toAar(), oldMapping, new.toAar(), newMapping)
        Type.Jar -> BinaryDiff.ofJar(old.toJar(), oldMapping, new.toJar(), newMapping)
      }
    }
  }

  private enum class ReportType {
    Text, Html, None
  }

  private val outputOptions by object : OptionGroup(name = "Output options") {
    private val text by option(
          help = "File to write text report. Note: Specifying this option will disable printing the text report to standard out by default. Specify '--stdout text' to restore that behavior.",
          metavar = "FILE"
        )
        .path(fileSystem = outputFs)
    private val html by option(
          help = "File to write HTML report. Note: Specifying this option will disable printing the text report to standard out by default. Specify '--stdout text' to restore that behavior.",
          metavar = "FILE"
        )
        .path(fileSystem = outputFs)
    private val stdout by option(
          help = "Report to print to standard out. By default, The text report will be printed to standard out ONLY when neither --text nor --html are specified."
        )
        .choice("text" to ReportType.Text, "html" to ReportType.Html)
        .defaultLazy {
          if (text == null && html == null) {
            ReportType.Text
          } else {
            ReportType.None
          }
        }

    fun write(diff: BinaryDiff) {
      val textReport by lazy(NONE) { diff.toTextReport().toString() }
      val htmlReport by lazy(NONE) { diff.toHtmlReport().toString() }

      text?.writeText(textReport)
      html?.writeText(htmlReport)

      val printReport = when (stdout) {
        ReportType.Text -> textReport
        ReportType.Html -> htmlReport
        ReportType.None -> null
      }
      printReport?.let(output::println)
    }
  }

  private val old by argument("OLD", help = "Old input file.")
      .path(exists = true, folderOkay = false, readable = true, fileSystem = inputFs)

  private val new by argument("NEW", help = "New input file.")
      .path(exists = true, folderOkay = false, readable = true, fileSystem = inputFs)

  override fun run() {
    val diff = inputOptions.parse(old.asInput(), new.asInput())
    outputOptions.write(diff)
  }
}

private class MembersCommand(
  inputFs: FileSystem,
  private val stdout: PrintStream
) : CliktCommand(name = "members") {
  private val binary by argument("FILE", help = "Input file.")
      .path(exists = true, folderOkay = false, readable = true, fileSystem = inputFs)

  private val hideSyntheticNumbers by option("--hide-synthetic-numbers",
      help = "Remove synthetic numbers from type and method names. This is useful to prevent noise when diffing output.")
      .flag()

  private val type by option(help = "File type. Default is 'apk'.")
      .switch("--apk" to Type.Apk, "--aar" to Type.Aar, "--aab" to Type.Aab, "--jar" to Type.Jar)
      .default(Type.Apk)

  private val mode by option(help = "Items to display. Default is all (methods and fields).")
      .switch("--all" to Mode.All, "--methods" to Mode.Methods, "--fields" to Mode.Fields)
      .default(Mode.All)

  enum class Mode {
    All, Methods, Fields
  }

  override fun run() {
    val input = binary.asInput()

    val members = when (type) {
      Type.Apk -> input.toApk().dexes.flatMap(Dex::members)
      Type.Aab -> input.toAab().modules.flatMap(Module::dexes).flatMap(Dex::members)
      Type.Aar -> input.toAar().jars.flatMap(Jar::members)
      Type.Jar -> input.toJar().members
    }.toSet()

    val items = when (mode) {
      Mode.All -> members
      Mode.Methods -> members.filterIsInstance<Method>()
      Mode.Fields -> members.filterIsInstance<Field>()
    }

    val displayList = if (hideSyntheticNumbers) {
      items.map { it.withoutSyntheticSuffix() }
    } else {
      items
    }
    // Re-sort because rendering may subtly change ordering.
    displayList.map(Member::toString).sorted().forEach(stdout::println)
  }
}

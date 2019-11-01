@file:JvmName("Diffuse")

package com.jakewharton.diffuse

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.NoRunCliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.switch
import com.github.ajalt.clikt.parameters.types.path
import com.jakewharton.diffuse.Aab.Companion.toAab
import com.jakewharton.diffuse.Aab.Module
import com.jakewharton.diffuse.Aar.Companion.toAar
import com.jakewharton.diffuse.ApiMapping.Companion.toApiMapping
import com.jakewharton.diffuse.Apk.Companion.toApk
import com.jakewharton.diffuse.Jar.Companion.toJar
import com.jakewharton.diffuse.diff.BinaryDiff
import com.jakewharton.diffuse.io.Input.Companion.asInput

fun main(vararg args: String) {
  NoRunCliktCommand(name = "diffuse")
      .subcommands(DiffCommand(), MembersCommand())
      .main(args.toList())
}

private enum class Type {
  Apk, Aar, Aab, Jar
}

private class DiffCommand : CliktCommand(name = "diff") {
  private val old by argument("OLD", help = "Old input file.")
      .path(exists = true, folderOkay = false, readable = true)

  private val oldMappingPath by option("--old-mapping",
      help = "Mapping file produced by R8 or ProGuard.", metavar = "FILE")
      .path(exists = true, folderOkay = false, readable = true)

  private val new by argument("NEW", help = "New input file.")
      .path(exists = true, folderOkay = false, readable = true)

  private val newMappingPath by option("--new-mapping",
      help = "Mapping file produced by R8 or ProGuard.", metavar = "FILE")
      .path(exists = true, folderOkay = false, readable = true)

  private val type by option(help = "File type of OLD and NEW. Default is 'apk'.")
      .switch("--apk" to Type.Apk, "--aar" to Type.Aar, "--aab" to Type.Aab, "--jar" to Type.Jar)
      .default(Type.Apk)

  override fun run() {
    val oldInput = old.asInput()
    val oldMapping = oldMappingPath?.asInput()?.toApiMapping() ?: ApiMapping.EMPTY
    val newInput = new.asInput()
    val newMapping = newMappingPath?.asInput()?.toApiMapping() ?: ApiMapping.EMPTY
    val diff = when (type) {
      Type.Apk -> {
        BinaryDiff.ofApk(oldInput.toApk(), oldMapping, newInput.toApk(), newMapping)
      }
      Type.Aab -> {
        BinaryDiff.ofAab(oldInput.toAab(), newInput.toAab())
      }
      Type.Aar -> {
        BinaryDiff.ofAar(oldInput.toAar(), oldMapping, newInput.toAar(), newMapping)
      }
      Type.Jar -> {
        BinaryDiff.ofJar(oldInput.toJar(), oldMapping, newInput.toJar(), newMapping)
      }
    }
    println(diff.toTextReport())
  }
}

private class MembersCommand : CliktCommand(name = "members") {
  private val binary by argument("FILE", help = "Input file.")
      .path(exists = true, folderOkay = false, readable = true)

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
    displayList.map(Member::toString).sorted().forEach(::println)
  }
}

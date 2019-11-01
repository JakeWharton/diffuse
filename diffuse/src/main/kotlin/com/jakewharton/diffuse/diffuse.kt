@file:JvmName("Diffuse")

package com.jakewharton.diffuse

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.NoRunCliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.switch
import com.github.ajalt.clikt.parameters.types.path
import com.jakewharton.diffuse.Aab.Companion.toAab
import com.jakewharton.diffuse.Aar.Companion.toAar
import com.jakewharton.diffuse.ApiMapping.Companion.toApiMapping
import com.jakewharton.diffuse.Apk.Companion.toApk
import com.jakewharton.diffuse.Jar.Companion.toJar
import com.jakewharton.diffuse.diff.BinaryDiff
import com.jakewharton.diffuse.io.Input.Companion.asInput

fun main(vararg args: String) {
  NoRunCliktCommand(name = "diffuse")
      .subcommands(DiffCommand())
      .main(args.toList())
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

  enum class Type {
    Apk, Aar, Aab, Jar
  }

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

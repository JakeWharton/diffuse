@file:JvmName("Diffuse")

package com.jakewharton.diffuse

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.switch
import com.github.ajalt.clikt.parameters.types.path
import com.jakewharton.dex.ApiMapping
import com.jakewharton.dex.ApiMapping.Companion.toApiMapping
import com.jakewharton.diffuse.Aar.Companion.toAar
import com.jakewharton.diffuse.Apk.Companion.toApk

fun main(vararg args: String) {
  Command().main(args.toList())
}

fun apkDiff(
  oldApk: Apk,
  oldMapping: ApiMapping = ApiMapping.EMPTY,
  newApk: Apk,
  newMapping: ApiMapping = ApiMapping.EMPTY
): BinaryDiff = ApkDiff(oldApk, oldMapping, newApk, newMapping)

fun aarDiff(
  oldAar: Aar,
  oldMapping: ApiMapping = ApiMapping.EMPTY,
  newAar: Aar,
  newMapping: ApiMapping = ApiMapping.EMPTY
): BinaryDiff = AarDiff(oldAar, oldMapping, newAar, newMapping)

private class Command : CliktCommand(name = "diffuse") {
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

  private val mode by option(help = "File type of OLD and NEW. Default is 'apk'.")
      .switch("--apk" to Mode.Apk, "--aar" to Mode.Aar, "--aab" to Mode.Aab, "--jar" to Mode.Jar)
      .default(Mode.Apk)

  enum class Mode {
    Apk, Aar, Aab, Jar
  }

  override fun run() {
    val oldMapping = oldMappingPath?.toApiMapping() ?: ApiMapping.EMPTY
    val newMapping = newMappingPath?.toApiMapping() ?: ApiMapping.EMPTY
    val diff = when (mode) {
      Mode.Apk -> {
        apkDiff(old.toApk(), oldMapping, new.toApk(), newMapping)
      }
      Mode.Aab -> {
        TODO(".aab files not yet supported")
      }
      Mode.Aar -> {
        aarDiff(old.toAar(), oldMapping, new.toAar(), newMapping)
      }
      Mode.Jar -> {
        TODO(".jar files not yet supported")
      }
    }
    println(diff.toTextReport())
  }
}

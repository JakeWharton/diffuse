@file:JvmName("Diffuse")

package com.jakewharton.diffuse

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.jakewharton.dex.ApiMapping
import com.jakewharton.dex.ApiMapping.Companion.toApiMapping
import com.jakewharton.diffuse.Apk.Companion.toApk
import java.nio.file.Paths

fun main(vararg args: String) {
  Command().main(args.toList())
}

private class Command : CliktCommand(name = "diffuse") {
  private val oldApk by option("--old", help = "Old APK", metavar = "FILE")
      .convert { Paths.get(it).toApk() }
      .required()

  private val oldMapping: ApiMapping by option("--old-mapping",
      help = "Mapping file produced by R8 or ProGuard", metavar = "FILE")
      .convert { Paths.get(it).toApiMapping() }
      .default(ApiMapping.EMPTY)

  private val newApk by option("--new", help = "New APK", metavar = "FILE")
      .convert { Paths.get(it).toApk() }
      .required()

  private val newMapping: ApiMapping by option("--new-mapping",
      help = "Mapping file produced by R8 or ProGuard", metavar = "FILE")
      .convert { Paths.get(it).toApiMapping() }
      .default(ApiMapping.EMPTY)

  override fun run() {
    println(ApkDiff(oldApk, oldMapping, newApk, newMapping).toTextReport())
  }
}

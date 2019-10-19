@file:JvmName("Diffuse")

package com.jakewharton.diffuse

import com.github.ajalt.clikt.core.CliktCommand
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
import com.jakewharton.diffuse.diff.AabDiff
import com.jakewharton.diffuse.diff.AarDiff
import com.jakewharton.diffuse.diff.ApkDiff
import com.jakewharton.diffuse.diff.BinaryDiff
import com.jakewharton.diffuse.diff.JarDiff
import com.jakewharton.diffuse.io.Input.Companion.asInput

fun main(vararg args: String) {
  DiffuseCommand().main(args.toList())
}

fun apkDiff(
  oldApk: Apk,
  oldMapping: ApiMapping = ApiMapping.EMPTY,
  newApk: Apk,
  newMapping: ApiMapping = ApiMapping.EMPTY
): BinaryDiff = ApkDiff(oldApk, oldMapping, newApk, newMapping)

fun aabDiff(
  oldAab: Aab,
  newAab: Aab
): BinaryDiff = AabDiff(oldAab, newAab)

fun aarDiff(
  oldAar: Aar,
  oldMapping: ApiMapping = ApiMapping.EMPTY,
  newAar: Aar,
  newMapping: ApiMapping = ApiMapping.EMPTY
): BinaryDiff = AarDiff(oldAar, oldMapping, newAar, newMapping)

fun jarDiff(
  oldJar: Jar,
  oldMapping: ApiMapping = ApiMapping.EMPTY,
  newJar: Jar,
  newMapping: ApiMapping = ApiMapping.EMPTY
): BinaryDiff = JarDiff(oldJar, oldMapping, newJar, newMapping)

private class DiffuseCommand : CliktCommand(name = "diffuse") {
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
        apkDiff(oldInput.toApk(), oldMapping, newInput.toApk(), newMapping)
      }
      Type.Aab -> {
        aabDiff(oldInput.toAab(), newInput.toAab())
      }
      Type.Aar -> {
        aarDiff(oldInput.toAar(), oldMapping, newInput.toAar(), newMapping)
      }
      Type.Jar -> {
        jarDiff(oldInput.toJar(), oldMapping, newInput.toJar(), newMapping)
      }
    }
    println(diff.toTextReport())
  }
}

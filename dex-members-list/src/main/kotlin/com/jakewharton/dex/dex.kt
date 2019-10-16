package com.jakewharton.dex

import com.android.dex.Dex
import com.android.tools.r8.D8
import com.android.tools.r8.D8Command
import com.android.tools.r8.DexIndexedConsumer
import com.android.tools.r8.DiagnosticsHandler
import com.android.tools.r8.origin.Origin
import com.jakewharton.dex.DexParser.Desugaring
import java.io.ByteArrayInputStream
import java.util.zip.ZipInputStream

private val CLASS_MAGIC = byteArrayOf(0xCA.toByte(), 0xFE.toByte(), 0xBA.toByte(), 0xBE.toByte())
private val DEX_MAGIC = byteArrayOf(0x64, 0x65, 0x78, 0x0a, 0x30, 0x33, 0x35, 0x00)

internal fun Iterable<ByteArray>.toDexes(desugaring: Desugaring): List<Dex> {
  val classes = mutableListOf<ByteArray>()
  val dexes = mutableListOf<ByteArray>()

  for (input in this) {
    if (input.startsWith(DEX_MAGIC)) {
      dexes += input
    } else if (input.startsWith(CLASS_MAGIC)) {
      classes += input
    } else {
      ZipInputStream(ByteArrayInputStream(input)).use { zis ->
        zis.entries().forEach {
          if (it.name.endsWith(".dex")) {
            dexes += zis.readBytes()
          } else if (it.name.endsWith(".class") && !it.name.startsWith("META-INF/")) {
            classes += zis.readBytes()
          } else if (it.name.endsWith(".jar")) {
            ZipInputStream(ByteArrayInputStream(zis.readBytes())).use { jar ->
              jar.entries().forEach {
                if (it.name.endsWith(".class") && !it.name.startsWith("META-INF/")) {
                  classes += jar.readBytes()
                }
              }
            }
          }
        }
      }
    }
  }

  if (classes.isNotEmpty()) {
    dexes += compileClassesWithD8(classes, desugaring)
  }
  return dexes.map(::Dex)
}

private fun compileClassesWithD8(
  bytes: List<ByteArray>,
  desugaring: Desugaring
): List<ByteArray> {
  val builder = D8Command.builder()

  builder.minApiLevel = desugaring.minApiLevel
  builder.addLibraryFiles(desugaring.libraryJars)

  if (desugaring === Desugaring.DISABLED) {
    builder.disableDesugaring = true
  }

  bytes.forEach { builder.addClassProgramData(it, Origin.unknown()) }

  val bytesList = mutableListOf<ByteArray>()
  builder.programConsumer = object : DexIndexedConsumer {
    override fun finished(diagnostics: DiagnosticsHandler) = Unit
    override fun accept(
      index: Int,
      bytes: ByteArray,
      descriptors: Set<String>,
      diagnostics: DiagnosticsHandler?
    ) {
      bytesList += bytes
    }
  }

  D8.run(builder.build())

  return bytesList
}

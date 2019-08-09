package com.jakewharton.diffuse

import com.google.devrel.gmscore.tools.apk.arsc.ArscBlamer
import com.google.devrel.gmscore.tools.apk.arsc.BinaryResourceFile
import com.google.devrel.gmscore.tools.apk.arsc.ResourceTableChunk
import okio.ByteString

class Arsc private constructor(private val bytes: ByteString) {
  private val arscBlamer by lazy {
    val resourceFile = BinaryResourceFile(bytes.toByteArray())
    val chunk = resourceFile.chunks.single()
    check(chunk is ResourceTableChunk) { "Root arsc chunk is not a resource table "}
    ArscBlamer(chunk).apply { blame() }
  }

  val configs get() = arscBlamer.typeChunks.map { it.typeName }

  // TODO map this to model object?
  val entries get() = arscBlamer.resourceEntries.keySet().map { "${it.packageName()} ${it.typeName()} ${it.entryName()}" }

  companion object {
    @JvmStatic
    @JvmName("create")
    fun ByteString.toArsc() = Arsc(this)
  }
}

package com.jakewharton.diffuse

import com.google.devrel.gmscore.tools.apk.arsc.ArscBlamer
import com.google.devrel.gmscore.tools.apk.arsc.ArscBlamer.ResourceEntry
import com.google.devrel.gmscore.tools.apk.arsc.BinaryResourceFile
import com.google.devrel.gmscore.tools.apk.arsc.ResourceTableChunk

class Arsc private constructor(
  val configs: List<String>,
  val entries: List<Entry>
) {
  data class Entry(
    val type: String,
    val name: String
  ) : Comparable<Entry> {
    override fun compareTo(other: Entry) = comparable.compare(this, other)
    override fun toString() = "$type/$name"

    private companion object {
      private val comparable = compareBy(Entry::type, Entry::name)
    }
  }

  companion object {
    private fun ResourceEntry.toEntry() = Entry(typeName(), entryName())

    @JvmStatic
    @JvmName("create")
    fun BinaryResourceFile.toArsc(): Arsc {
      val chunk = chunks.single()
      check(chunk is ResourceTableChunk) { "Root arsc chunk is not a resource table "}
      val arscBlamer = ArscBlamer(chunk).apply { blame() }
      val configs = arscBlamer.typeChunks.map { it.typeName }
      val entries = arscBlamer.resourceEntries.keySet().map { it.toEntry() }
      return Arsc(configs, entries)
    }
  }
}

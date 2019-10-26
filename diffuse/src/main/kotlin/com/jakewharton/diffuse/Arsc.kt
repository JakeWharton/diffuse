package com.jakewharton.diffuse

import com.google.devrel.gmscore.tools.apk.arsc.BinaryResourceFile
import com.google.devrel.gmscore.tools.apk.arsc.ResourceTableChunk

class Arsc private constructor(
  val configs: List<String>,
  val entries: Map<Int, Entry>
) {
  data class Entry(
    val type: String,
    val name: String
  ) : Comparable<Entry> {
    override fun compareTo(other: Entry) = comparator.compare(this, other)
    override fun toString() = "$type/$name"

    private companion object {
      private val comparator = compareBy(Entry::type, Entry::name)
    }
  }

  companion object {
    @JvmStatic
    @JvmName("create")
    fun BinaryResourceFile.toArsc(): Arsc {
      val chunk = chunks.single()
      check(chunk is ResourceTableChunk) { "Root arsc chunk is not a resource table " }

      val configs = mutableListOf<String>()
      val entries = mutableMapOf<Int, Entry>()
      for (`package` in chunk.packages) {
        val packageId = `package`.id

        for (typeChunk in `package`.typeChunks) {
          val typeId = typeChunk.id
          configs += typeChunk.typeName

          for ((entryId, entry) in typeChunk.entries) {
            // See BinaryResourceIdentifier for the source of this algorithm.
            val id = ((packageId and 0xFF) shl 24) or
                ((typeId and 0xFF) shl 16) or
                ((entryId and 0xFFFF))

            entries[id] = Entry(entry.typeName(), entry.key())
          }
        }
      }

      return Arsc(configs, entries)
    }
  }
}

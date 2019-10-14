package com.jakewharton.diffuse

import com.jakewharton.diffuse.io.Input

class Aab private constructor(
  override val filename: String?
  // TODO val baseModule: Module,
  //  val featureModules: Map<String, Module>
) : Binary {
  // TODO class Module private constructor(
  //  val files: ArchiveFiles,
  //  val manifest: AndroidManifest,
  //  val dexes: List<Dex>
  //)

  companion object {
    @JvmStatic
    @JvmName("parse")
    fun Input.toAab(): Aab {
      toZip().use { zip ->
        // TODO lots!
        return Aab(name)
      }
    }
  }
}

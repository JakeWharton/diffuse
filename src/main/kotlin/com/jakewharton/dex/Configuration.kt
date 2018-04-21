package com.jakewharton.dex

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.mainBody
import java.io.FileInputStream

internal class Configuration private constructor(parser: ArgParser) {
  val hideSyntheticNumbers by parser.flagging("--hide-synthetic-numbers",
      help = "Remove number suffixes from synthetic accessor methods. This is useful to prevent noise when diffing output.")

  val legacyDx by parser.flagging("--legacy-dx",
      help = "Use legacy 'dx' dex compiler instead of D8.")

  val inputs by parser.positionalList("FILE",
      ".apk, .aar, .jar, .dex, and/or .class files to process. STDIN is used when no files are provided.",
      0..Int.MAX_VALUE)

  fun loadInputs() = inputs
      .map(::FileInputStream)
      .defaultIfEmpty(System.`in`)
      .map { it.use { it.readBytes() } }
      .toList()

  companion object {
    fun load(name: String, vararg args: String) = mainBody(name) {
      val parser = ArgParser(args)
      Configuration(parser).also { parser.force() }
    }
  }
}

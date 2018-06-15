@file:JvmName("DexFields")

package com.jakewharton.dex

import java.io.File

fun main(vararg args: String) {
  FieldCommand().main(args.toList())
}

/** List field references in the files of any `.dex`, `.class`, `.jar`, `.aar`, or `.apk`. */
@Deprecated("Use DexParser",
    ReplaceWith("DexParser.fromFiles(*files).listFields()", "com.jakewharton.dex.DexParser"))
@JvmName("list")
fun dexFields(vararg files: File) = DexParser.fromFiles(*files).listFields()

/** List field references in the bytes of any `.dex`, `.class`, `.jar`, `.aar`, or `.apk`. */
@Deprecated("Use DexParser",
    ReplaceWith("DexParser.fromBytes(bytes).listFields()", "com.jakewharton.dex.DexParser"))
@JvmName("list")
fun dexFields(bytes: ByteArray) = DexParser.fromBytes(bytes).listFields()

/** List field references in the bytes of any `.dex`, `.class`, `.jar`, `.aar`, or `.apk`. */
@Deprecated("Use DexParser",
    ReplaceWith("DexParser.fromBytes(bytes).listFields()", "com.jakewharton.dex.DexParser"))
@JvmName("list")
fun dexFields(bytes: Iterable<ByteArray>) = DexParser.fromBytes(bytes).listFields()

@file:JvmName("DexMethods")

package com.jakewharton.dex

import java.io.File

fun main(vararg args: String) {
  MethodCommand().main(args.toList())
}

/** List method references in the files of any `.dex`, `.class`, `.jar`, `.aar`, or `.apk`. */
@Deprecated("Use DexParser",
    ReplaceWith("DexParser.fromFiles(*files).listMethods()", "com.jakewharton.dex.DexParser"))
@JvmName("list")
fun dexMethods(vararg files: File) = DexParser.fromFiles(*files).listMethods()

/** List method references in the bytes of any `.dex`, `.class`, `.jar`, `.aar`, or `.apk`. */
@Deprecated("Use DexParser",
    ReplaceWith("DexParser.fromBytes(bytes).listMethods()", "com.jakewharton.dex.DexParser"))
@JvmName("list")
fun dexMethods(bytes: ByteArray) = DexParser.fromBytes(bytes).listMethods()

/** List method references in the bytes of any `.dex`, `.class`, `.jar`, `.aar`, or `.apk`. */
@Deprecated("Use DexParser",
    ReplaceWith("DexParser.fromBytes(bytes).listMethods()", "com.jakewharton.dex.DexParser"))
@JvmName("list")
fun dexMethods(bytes: Iterable<ByteArray>) = DexParser.fromBytes(bytes).listMethods()

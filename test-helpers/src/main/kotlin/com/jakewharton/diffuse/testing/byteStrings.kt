package com.jakewharton.diffuse.testing

import okio.ByteString
import okio.ByteString.Companion.decodeHex

fun String.decodeHexWithWhitespace(): ByteString {
  return replace(Regex("\\s"), "").decodeHex()
}

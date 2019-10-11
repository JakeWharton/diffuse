package com.jakewharton.diffuse

import okio.ByteString

internal class SignaturesDiff(
  val oldSignatures: Signatures,
  val newSignatures: Signatures
) {
  val changed = oldSignatures != newSignatures
}

internal fun SignaturesDiff.toDetailReport() = buildString {
  appendln()
  appendln(diffuseTable {
    header {
      row("", "old", "new")
    }
    if (oldSignatures.v1.isNotEmpty() || newSignatures.v1.isNotEmpty()) {
      row("V1",
          oldSignatures.v1.joinToString("\n", transform = ByteString::hex),
          newSignatures.v1.joinToString("\n", transform = ByteString::hex))
    }
    if (oldSignatures.v2.isNotEmpty() || newSignatures.v2.isNotEmpty()) {
      row("V2",
          oldSignatures.v2.joinToString("\n", transform = ByteString::hex),
          newSignatures.v2.joinToString("\n", transform = ByteString::hex))
    }
    if (oldSignatures.v3.isNotEmpty() || newSignatures.v3.isNotEmpty()) {
      row("V3",
          oldSignatures.v3.joinToString("\n", transform = ByteString::hex),
          newSignatures.v3.joinToString("\n", transform = ByteString::hex))
    }
  })
}

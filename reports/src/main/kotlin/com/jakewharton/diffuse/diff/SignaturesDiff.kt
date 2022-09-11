package com.jakewharton.diffuse.diff

import com.jakewharton.diffuse.diffuseTable
import com.jakewharton.diffuse.format.Signatures
import com.jakewharton.picnic.TextAlignment.TopRight
import okio.ByteString

internal class SignaturesDiff(
  val oldSignatures: Signatures,
  val newSignatures: Signatures,
) {
  val changed = oldSignatures != newSignatures
}

internal fun SignaturesDiff.toDetailReport() = buildString {
  appendLine()
  appendLine(
    diffuseTable {
      header {
        row("SIGNATURES", "old", "new")
      }
      if (oldSignatures.v1.isNotEmpty() || newSignatures.v1.isNotEmpty()) {
        row {
          cell("V1") {
            alignment = TopRight
          }
          cell(oldSignatures.v1.joinToString("\n", transform = ByteString::hex))
          cell(newSignatures.v1.joinToString("\n", transform = ByteString::hex))
        }
      }
      if (oldSignatures.v2.isNotEmpty() || newSignatures.v2.isNotEmpty()) {
        row {
          cell("V2") {
            alignment = TopRight
          }
          cell(oldSignatures.v2.joinToString("\n", transform = ByteString::hex))
          cell(newSignatures.v2.joinToString("\n", transform = ByteString::hex))
        }
      }
      if (oldSignatures.v3.isNotEmpty() || newSignatures.v3.isNotEmpty()) {
        row {
          cell("V3") {
            alignment = TopRight
          }
          cell(oldSignatures.v3.joinToString("\n", transform = ByteString::hex))
          cell(newSignatures.v3.joinToString("\n", transform = ByteString::hex))
        }
      }
      if (oldSignatures.v4.isNotEmpty() || newSignatures.v4.isNotEmpty()) {
        row {
          cell("V4") {
            alignment = TopRight
          }
          cell(oldSignatures.v4.joinToString("\n", transform = ByteString::hex))
          cell(newSignatures.v4.joinToString("\n", transform = ByteString::hex))
        }
      }
    },
  )
}

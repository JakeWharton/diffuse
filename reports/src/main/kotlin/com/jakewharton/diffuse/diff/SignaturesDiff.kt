package com.jakewharton.diffuse.diff

import com.jakewharton.diffuse.diffuseTable
import com.jakewharton.diffuse.format.Signatures
import com.jakewharton.picnic.TextAlignment.TopRight
import kotlinx.html.FlowContent
import kotlinx.html.style
import kotlinx.html.table
import kotlinx.html.td
import kotlinx.html.thead
import kotlinx.html.tr
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

internal fun FlowContent.toDetailReport(diff: SignaturesDiff) {
  table {
    thead {
      tr {
        td { +"SIGNATURES" }
        td { +"old" }
        td { +"new" }
      }
    }

    if (diff.oldSignatures.v1.isNotEmpty() || diff.newSignatures.v1.isNotEmpty()) {
      tr {
        td {
          style = "text-align: right; vertical-align: top;"
          +"V1"
        }

        td { +diff.oldSignatures.v1.joinToString("\n", transform = ByteString::hex) }
        td { +diff.newSignatures.v1.joinToString("\n", transform = ByteString::hex) }
      }
    }

    if (diff.oldSignatures.v2.isNotEmpty() || diff.newSignatures.v2.isNotEmpty()) {
      tr {
        td {
          style = "text-align: right; vertical-align: top;"
          +"V2"
        }

        td { +diff.oldSignatures.v2.joinToString("\n", transform = ByteString::hex) }
        td { +diff.newSignatures.v2.joinToString("\n", transform = ByteString::hex) }
      }
    }

    if (diff.oldSignatures.v3.isNotEmpty() || diff.newSignatures.v3.isNotEmpty()) {
      tr {
        td {
          style = "text-align: right; vertical-align: top;"
          +"V3"
        }

        td { +diff.oldSignatures.v3.joinToString("\n", transform = ByteString::hex) }
        td { +diff.newSignatures.v3.joinToString("\n", transform = ByteString::hex) }
      }
    }

    if (diff.oldSignatures.v4.isNotEmpty() || diff.newSignatures.v4.isNotEmpty()) {
      tr {
        td {
          style = "text-align: right; vertical-align: top;"
          +"V4"
        }

        td { +diff.oldSignatures.v4.joinToString("\n", transform = ByteString::hex) }
        td { +diff.newSignatures.v4.joinToString("\n", transform = ByteString::hex) }
      }
    }
  }
}

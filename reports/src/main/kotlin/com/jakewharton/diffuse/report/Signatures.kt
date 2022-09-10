package com.jakewharton.diffuse.report

import com.jakewharton.diffuse.format.Signatures

internal fun Signatures.toSummaryString(): String {
  if (v1.isEmpty() && v2.isEmpty() && v3.isEmpty() && v4.isEmpty()) {
    return "none"
  }
  return buildString {
    if (v1.isNotEmpty()) {
      append("V1")
      if (v1.size > 1) {
        append(" (x")
        append(v1.size)
        append(')')
      }
    }
    if (v2.isNotEmpty()) {
      if (isNotEmpty()) {
        append(", ")
      }
      append("V2")
      if (v2.size > 1) {
        append(" (x")
        append(v2.size)
        append(')')
      }
    }
    if (v3.isNotEmpty()) {
      if (isNotEmpty()) {
        append(", ")
      }
      append("V3")
      if (v3.size > 1) {
        append(" (x")
        append(v3.size)
        append(')')
      }
    }
    if (v4.isNotEmpty()) {
      if (isNotEmpty()) {
        append(", ")
      }
      append("V4")
      if (v4.size > 1) {
        append(" (x")
        append(v4.size)
        append(')')
      }
    }
  }
}

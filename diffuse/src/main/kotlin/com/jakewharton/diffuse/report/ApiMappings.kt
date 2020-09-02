package com.jakewharton.diffuse.report

import com.jakewharton.diffuse.format.ApiMapping

internal fun ApiMapping.toSummaryString(): String {
  if (this === ApiMapping.EMPTY) {
    return "not provided"
  }
  return buildString {
    append(types.toUnitString("types", 1 to "type"))
    if (types > 0) {
      append(", ")
      append(fields.toUnitString("fields", 1 to "field"))
      append(", ")
      append(methods.toUnitString("methods", 1 to "method"))
    }
  }
}

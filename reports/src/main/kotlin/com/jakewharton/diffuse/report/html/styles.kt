package com.jakewharton.diffuse.report.html

import kotlinx.html.HEAD
import kotlinx.html.link
import kotlinx.html.style
import kotlinx.html.unsafe

internal fun HEAD.applyStyles() {
  link("https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css", "stylesheet") {
    integrity = "sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjXh0JMhjY6hW+ALEwIH"
    attributes["crossorigin"] = "anonymous"
  }

  style("text/css") {
    unsafe {
      raw(
        """
          table {
            border-collapse:collapse;
            border:1px solid var(--bs-body-color);
          }

          table td {
            border:1px solid var(--bs-body-color);
            padding: 4px;
          }
          
          body {
            margin: 0 16pt;
          }
        """.trimIndent(),
      )
    }
  }
}

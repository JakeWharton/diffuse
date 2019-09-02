package com.jakewharton.diffuse

interface Diff {
  fun toTextReport(): DiffReport
}

interface DiffReport {
  fun write(appendable: Appendable)
}

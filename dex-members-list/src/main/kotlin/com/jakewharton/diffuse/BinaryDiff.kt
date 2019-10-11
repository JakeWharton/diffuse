package com.jakewharton.diffuse

interface BinaryDiff {
  fun toTextReport(): DiffReport
}

interface DiffReport {
  fun write(appendable: Appendable)
}

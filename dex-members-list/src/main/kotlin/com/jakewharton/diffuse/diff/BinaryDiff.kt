package com.jakewharton.diffuse.diff

interface BinaryDiff {
  fun toTextReport(): DiffReport
}

interface DiffReport {
  fun write(appendable: Appendable)
}

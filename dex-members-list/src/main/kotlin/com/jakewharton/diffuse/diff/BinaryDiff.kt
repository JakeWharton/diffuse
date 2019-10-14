package com.jakewharton.diffuse.diff

import com.jakewharton.diffuse.report.DiffReport

interface BinaryDiff {
  fun toTextReport(): DiffReport
}

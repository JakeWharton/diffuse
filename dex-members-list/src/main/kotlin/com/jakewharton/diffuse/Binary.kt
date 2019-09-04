package com.jakewharton.diffuse

import okio.ByteString

interface Binary {
  val filename: String?
  val bytes: ByteString
}

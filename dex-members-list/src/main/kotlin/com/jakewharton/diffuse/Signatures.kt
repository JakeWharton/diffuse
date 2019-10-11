package com.jakewharton.diffuse

import okio.ByteString

data class Signatures(
  val v1: List<ByteString>,
  val v2: List<ByteString>,
  val v3: List<ByteString>
)

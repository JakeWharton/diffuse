package com.jakewharton.diffuse.io

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.diffuse.testing.decodeHexWithWhitespace
import org.junit.Test

class ZipTest {
  @Test fun dataDescriptorSize() {
    val zip = """
      504b 0304 2d00 0800 0800 24be 3a58 0000
      0000 ffff ffff ffff ffff 0100 0000 2dcb
      48cd c9c9 0700 504b 0708 86a6 1036 0700
      0000 0500 0000 504b 0102 1e03 2d00 0800
      0800 24be 3a58 86a6 1036 0700 0000 0500
      0000 0100 0000 0000 0000 0100 0000 b011
      0000 0000 2d50 4b05 0600 0000 0001 0001
      002f 0000 0036 0000 0000 00
    """.decodeHexWithWhitespace().toZip()
    val entry = zip.entries.single()

    assertThat(entry.compressedSize).isEqualTo(Size(7))
    assertThat(entry.uncompressedSize).isEqualTo(Size(5))
  }
}

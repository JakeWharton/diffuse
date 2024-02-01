package com.jakewharton.diffuse.format

import assertk.assertThat
import assertk.assertions.isEmpty
import com.jakewharton.diffuse.format.Arsc.Companion.toArsc
import com.jakewharton.diffuse.io.Input.Companion.asInput
import com.jakewharton.diffuse.testing.decodeHexWithWhitespace
import org.junit.Test

class ArscTest {
  @Test fun agp7EmptyResourceTable() {
    val arsc = """
      0200 0c00 2800 0000 0000 0000 0100 1c00
      1c00 0000 0000 0000 0000 0000 0001 0000
      1c00 0000 0000 0000
    """.decodeHexWithWhitespace().asInput("resources.arsc").toArsc()

    assertThat(arsc.configs).isEmpty()
    assertThat(arsc.entries).isEmpty()
  }

  @Test fun agp4_2_2EmptyResourceTable() {
    val arsc = """
      0200 0c00 8001 0000 0100 0000 0100 1c00
      1c00 0000 0000 0000 0000 0000 0001 0000
      1c00 0000 0000 0000 0002 2001 5801 0000
      7f00 0000 7200 6500 7400 7200 6f00 6600
      6900 7400 3200 2e00 6100 6e00 6400 7200
      6f00 6900 6400 2e00 7400 6500 7300 7400
      0000 0000 0000 0000 0000 0000 0000 0000
      0000 0000 0000 0000 0000 0000 0000 0000
      0000 0000 0000 0000 0000 0000 0000 0000
      0000 0000 0000 0000 0000 0000 0000 0000
      0000 0000 0000 0000 0000 0000 0000 0000
      0000 0000 0000 0000 0000 0000 0000 0000
      0000 0000 0000 0000 0000 0000 0000 0000
      0000 0000 0000 0000 0000 0000 0000 0000
      0000 0000 0000 0000 0000 0000 0000 0000
      0000 0000 0000 0000 0000 0000 0000 0000
      0000 0000 0000 0000 0000 0000 0000 0000
      0000 0000 0000 0000 0000 0000 0000 0000
      0000 0000 0000 0000 0000 0000 0000 0000
      0000 0000 2001 0000 0000 0000 3c01 0000
      0000 0000 0000 0000 0100 1c00 1c00 0000
      0000 0000 0000 0000 0000 0000 1c00 0000
      0000 0000 0100 1c00 1c00 0000 0000 0000
      0000 0000 0001 0000 1c00 0000 0000 0000                   
    """.decodeHexWithWhitespace().asInput("resources.arsc").toArsc()

    assertThat(arsc.configs).isEmpty()
    assertThat(arsc.entries).isEmpty()
  }
}

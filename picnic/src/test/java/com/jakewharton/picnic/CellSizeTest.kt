package com.jakewharton.picnic

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CellSizeTest {
  @Test fun height() {
    val table = table {
      row("1\n2\n3", "1\n2", "1")
      row("1\n2", "1", "1\n2\n3")
      row("1", "1\n2\n3", "1\n2")
    }

    assertThat(table.renderText()).isEqualTo("""
      |111
      |22 
      |3  
      |111
      |2 2
      |  3
      |111
      | 22
      | 3 
      |""".trimMargin())
  }

  @Test fun width() {
    val table = table {
      row("123", "12", "1")
      row("12", "1", "123")
      row("1", "123", "12")
    }

    assertThat(table.renderText()).isEqualTo("""
      |12312 1  
      |12 1  123
      |1  12312 
      |""".trimMargin())
  }

  @Test fun widthAndHeight() {
    val table = table {
      row("123\n12\n1", "12\n1", "1")
      row("12\n1", "1", "123\n12\n1")
      row("1", "123\n12\n1", "12\n1")
    }

    assertThat(table.renderText()).isEqualTo("""
      |12312 1  
      |12 1     
      |1        
      |12 1  123
      |1     12 
      |      1  
      |1  12312 
      |   12 1  
      |   1     
      |""".trimMargin())
  }
}

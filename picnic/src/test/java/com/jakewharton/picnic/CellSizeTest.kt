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

  @Test fun heightWithVerticalPadding() {
    val table = table {
      row {
        cell("1")
        cell {
          paddingTop = 1
          "2"
        }
        cell {
          paddingBottom = 1
          "3"
        }
      }
      row {
        cell("1")
        cell {
          paddingTop = 1
          paddingBottom = 1
          "2"
        }
        cell {
          padding(top = 1, bottom = 1)
          "3"
        }
      }
    }

    assertThat(table.renderText()).isEqualTo("""
      |1 3
      | 2 
      |1  
      | 23
      |   
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

  @Test fun widthWithHorizontalPadding() {
    val table = table {
      row {
        cell("1")
        cell {
          paddingLeft = 2
          "2"
        }
        cell {
          paddingRight = 2
          "3"
        }
      }
      row {
        cell("1")
        cell {
          paddingLeft = 1
          paddingRight = 1
          "2"
        }
        cell {
          padding(left = 1, right = 1)
          "3"
        }
      }
    }

    assertThat(table.renderText()).isEqualTo("""
      |1  23  
      |1 2  3 
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

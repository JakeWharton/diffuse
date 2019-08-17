package com.jakewharton.picnic

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CellBorderTest {
  @Test fun allCorners() {
    val table = table {
      row {
        cell {
          borderTop = true
          borderLeft = true
          borderRight = true
          " "
        }
        cell {
          borderTop = true
          borderLeft = true
          borderRight = true
          " "
        }
        cell {
          borderLeft = true
          borderRight = true
          " "
        }
      }
      row {
        cell {
          borderTop = true
          borderLeft = true
          borderRight = true
          borderBottom = true
          " "
        }
        cell {
          borderLeft = true
          borderBottom = true
          " "
        }
        cell {
          borderRight = true
          borderBottom = true
          " "
        }
      }
      row {
        cell {
          borderTop = true
          borderRight = true
          borderBottom = true
          " "
        }
        cell {
          borderTop = true
          borderLeft = true
          borderBottom = true
          " "
        }
        cell {
          borderTop = true
          " "
        }
      }
    }

    assertThat(table.renderText()).isEqualTo("""
      |┌─┬─┐ ╷
      |│ │ │ │
      |├─┤ ╵ │
      |│ │   │
      |└─┼───┘
      |  │    
      |╶─┴─╴  
      |""".trimMargin())

    assertThat(table.renderText(border = TextBorder.ASCII)).isEqualTo("""
      |+-+-+  
      || | | |
      |+-+   |
      || |   |
      |+-+---+
      |  |    
      | -+-   
      |""".trimMargin())
  }

  @Test fun adjacentRowBordersWithoutCorners() {
    val table = table {
      row {
        cell {
          borderBottom = true
          "1"
        }
        cell {
          borderBottom = true
          "2"
        }
      }
      row("3", "4")
    }

    assertThat(table.renderText()).isEqualTo("""
      |12
      |──
      |34
      |""".trimMargin())
  }

  @Test fun adjacentColumnBordersWithoutCorners() {
    val table = table {
      row {
        cell {
          borderRight = true
          "1"
        }
        cell("2")
      }
      row {
        cell {
          borderRight = true
          "3"
        }
        cell("4")
      }
    }

    assertThat(table.renderText()).isEqualTo("""
      |1│2
      |3│4
      |""".trimMargin())
  }
}

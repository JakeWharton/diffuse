package com.jakewharton.picnic

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CellBorderTest {
  @Test fun allCorners() {
    val table = table {
      row {
        cell(" ") {
          borderTop = true
          borderLeft = true
          borderRight = true
        }
        cell(" ") {
          borderTop = true
          borderLeft = true
          borderRight = true
        }
        cell(" ") {
          borderLeft = true
          borderRight = true
        }
      }
      row {
        cell(" ") {
          borderTop = true
          borderLeft = true
          borderRight = true
          borderBottom = true
        }
        cell(" ") {
          borderLeft = true
          borderBottom = true
        }
        cell(" ") {
          borderRight = true
          borderBottom = true
        }
      }
      row {
        cell(" ") {
          borderTop = true
          borderRight = true
          borderBottom = true
        }
        cell(" ") {
          borderTop = true
          borderLeft = true
          borderBottom = true
        }
        cell(" ") {
          borderTop = true
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
        cell(1) {
          borderBottom = true
        }
        cell(2) {
          borderBottom = true
        }
      }
      row(3, 4)
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
        cell(1) {
          borderRight = true
        }
        cell(2)
      }
      row {
        cell(3) {
          borderRight = true
        }
        cell(4)
      }
    }

    assertThat(table.renderText()).isEqualTo("""
      |1│2
      |3│4
      |""".trimMargin())
  }

  @Test fun rowSpanPushesBordersToTheRight() {
    val table = table {
      row {
        cell("A") {
          rowSpan = 2
          borderBottom = true
        }
        cell("B") {
          borderBottom = true
        }
      }
      row {
        cell("C") {
          borderBottom = true
        }
      }
    }

    assertThat(table.renderText()).isEqualTo("""
      |AB
      | ─
      | C
      |──
      |""".trimMargin())
  }

  @Test fun stylePropagation() {
    val table = table {
      cellStyle {
        border = true
      }
      body {
        cellStyle {
          borderTop = false
        }
        row {
          cellStyle {
            borderLeft = false
          }
          cell("A") {
            borderRight = false
          }
        }
      }
    }

    assertThat(table.renderText()).isEqualTo("""
      |A
      |─
      |""".trimMargin())
  }

  @Test fun tableStyleTakesPrecedenceOverCell() {
    val table = table {
      style {
        borderStyle = BorderStyle.Hidden
      }
      cellStyle {
        border = true
      }
      body {
        row("A", "B", "C")
        row("D", "E", "F")
        row("G", "H", "I")
      }
    }

    assertThat(table.renderText()).isEqualTo("""
      |A│B│C
      |─┼─┼─
      |D│E│F
      |─┼─┼─
      |G│H│I
      |""".trimMargin())
  }

  @Test fun tableStyleTakesPrecedenceOverCellWithRowAndColumnSpans() {
    val table = table {
      style {
        borderStyle = BorderStyle.Hidden
      }
      cellStyle {
        border = true
      }
      row {
        cell("1")
        cell("1")
        cell("2") {
          rowSpan = 2
          columnSpan = 2
        }
      }
      row("1", "1")
      row {
        cell("2") {
          rowSpan = 2
          columnSpan = 2
        }
        cell("1")
        cell("1")
      }
      row("1", "1")
    }

    assertThat(table.renderText()).isEqualTo("""
      |1│1│2  
      |─┼─┤   
      |1│1│   
      |─┴─┼─┬─
      |2  │1│1
      |   ├─┼─
      |   │1│1
      |""".trimMargin())
  }

  @Test fun borderLeftCalculationWithTableBorderHidden() {
    val table = table {
      style {
        borderStyle = BorderStyle.Hidden
      }
      cellStyle {
        borderLeft = true
      }
      row("1", "2", "3")
      row {
        cell("4") {
          columnSpan = 2
        }
        cell("5")
      }
      row {
        cell("6")
        cell("7") {
          columnSpan = 2
        }
      }
      row {
        cell("8") {
          columnSpan = 3
        }
      }
    }

    assertThat(table.renderText()).isEqualTo("""
      |1│2│3
      |4  │5
      |6│7  
      |8    
      |""".trimMargin())
  }

  @Test fun borderRightCalculationWithTableBorderHidden() {
    val table = table {
      style {
        borderStyle = BorderStyle.Hidden
      }
      cellStyle {
        borderRight = true
      }
      row("1", "2", "3")
      row {
        cell("4") {
          columnSpan = 2
        }
        cell("5")
      }
      row {
        cell("6")
        cell("7") {
          columnSpan = 2
        }
      }
      row {
        cell("8") {
          columnSpan = 3
        }
      }
    }

    assertThat(table.renderText()).isEqualTo("""
      |1│2│3
      |4  │5
      |6│7  
      |8    
      |""".trimMargin())
  }

  @Test fun borderTopCalculationWithTableBorderHidden() {
    val table = table {
      style {
        borderStyle = BorderStyle.Hidden
      }
      cellStyle {
        borderTop = true
      }
      row {
        cell("1")
        cell("2") {
          rowSpan = 2
        }
        cell("3")
        cell("4") {
          rowSpan = 3
        }
      }
      row {
        cell("5")
        cell("6") {
          rowSpan = 2
        }
      }
      row("7", "8")
    }

    assertThat(table.renderText()).isEqualTo("""
      |1234
      |─ ─ 
      |5 6 
      |──  
      |78  
      |""".trimMargin())
  }

  @Test fun borderBottomCalculationWithTableBorderHidden() {
    val table = table {
      style {
        borderStyle = BorderStyle.Hidden
      }
      cellStyle {
        borderBottom = true
      }
      row {
        cell("1")
        cell("2") {
          rowSpan = 2
        }
        cell("3")
        cell("4") {
          rowSpan = 3
        }
      }
      row {
        cell("5")
        cell("6") {
          rowSpan = 2
        }
      }
      row("7", "8")
    }

    assertThat(table.renderText()).isEqualTo("""
      |1234
      |─ ─ 
      |5 6 
      |──  
      |78  
      |""".trimMargin())
  }
}

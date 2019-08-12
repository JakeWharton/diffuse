package com.jakewharton.picnic

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CellSpanTest {
  @Test fun columnSpans() {
    val table = table {
      row {
        cell {
          columnSpan = 8
          "88888888"
        }
        cell("1")
      }
      row {
        repeat(2) {
          cell {
            columnSpan = 4
            "4444"
          }
        }
        cell("1")
      }
      row {
        repeat(4) {
          cell {
            columnSpan = 2
            "22"
          }
        }
        cell("1")
      }
      row {
        repeat(9) {
          cell("1")
        }
      }
    }

    assertThat(table.renderText()).isEqualTo("""
      |888888881
      |444444441
      |222222221
      |111111111
      |""".trimMargin())
  }

  @Test fun rowSpans() {
    val table = table {
      row {
        cell {
          rowSpan = 8
          "8\n8\n8\n8\n8\n8\n8\n8"
        }
        cell {
          rowSpan = 4
          "4\n4\n4\n4"
        }
        cell {
          rowSpan = 2
          "2\n2"
        }
        cell("1")
      }
      row("1")
      row {
        cell {
          rowSpan = 2
          "2\n2"
        }
        cell("1")
      }
      row("1")
      row {
        cell {
          rowSpan = 4
          "4\n4\n4\n4"
        }
        cell {
          rowSpan = 2
          "2\n2"
        }
        cell("1")
      }
      row("1")
      row {
        cell {
          rowSpan = 2
          "2\n2"
        }
        cell("1")
      }
      row("1")
      row {
        repeat(4) {
          cell("1")
        }
      }
    }

    assertThat(table.renderText()).isEqualTo("""
      |8421
      |8421
      |8421
      |8421
      |8421
      |8421
      |8421
      |8421
      |1111
      |""".trimMargin())
  }

  @Test fun rowAndColumnSpans() {
    val table = table {
      row {
        cell {
          rowSpan = 3
          columnSpan = 3
          "333\n333\n333"
        }
        cell("1")
        cell("1")
        cell("1")
      }
      row {
        cell {
          rowSpan = 2
          columnSpan = 2
          "22\n22"
        }
        cell("1")
      }
      row("1")
      row {
        cell {
          rowSpan = 2
          columnSpan = 2
          "22\n22"
        }
        cell {
          rowSpan = 3
          columnSpan = 3
          "333\n333\n333"
        }
        cell("1")
      }
      row("1")
      row("1", "1", "1")
      row("1", "1", "1", "1", "1", "1")
    }

    assertThat(table.renderText()).isEqualTo("""
      |333111
      |333221
      |333221
      |223331
      |223331
      |113331
      |111111
      |""".trimMargin())
  }
}

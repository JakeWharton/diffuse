package com.jakewharton.picnic

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CellSpanTest {
  @Test fun columnSpans() {
    val table = table {
      row {
        cell {
          columnSpan = 8
          "8"
        }
        cell("1")
      }
      row {
        repeat(2) {
          cell {
            columnSpan = 4
            "4"
          }
        }
        cell("1")
      }
      row {
        repeat(4) {
          cell {
            columnSpan = 2
            "2"
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

    assertThat(table.render()).isEqualTo("""
      |8       1
      |4   4   1
      |2 2 2 2 1
      |111111111
      |""".trimMargin())
  }

  @Test fun rowSpans() {
    val table = table {
      row {
        cell {
          rowSpan = 8
          "8"
        }
        cell {
          rowSpan = 4
          "4"
        }
        cell {
          rowSpan = 2
          "2"
        }
        cell("1")
      }
      row("1")
      row {
        cell {
          rowSpan = 2
          "2"
        }
        cell("1")
      }
      row("1")
      row {
        cell {
          rowSpan = 4
          "4"
        }
        cell {
          rowSpan = 2
          "2"
        }
        cell("1")
      }
      row("1")
      row {
        cell {
          rowSpan = 2
          "2"
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

    assertThat(table.render()).isEqualTo("""
      |8421
      |   1
      |  21
      |   1
      | 421
      |   1
      |  21
      |   1
      |1111
      |""".trimMargin())
  }

  @Test fun rowAndColumnSpans() {
    val table = table {
      row {
        cell {
          rowSpan = 3
          columnSpan = 3
          "3"
        }
        cell("1")
        cell("1")
        cell("1")
      }
      row {
        cell {
          rowSpan = 2
          columnSpan = 2
          "2"
        }
        cell("1")
      }
      row("1")
      row {
        cell {
          rowSpan = 2
          columnSpan = 2
          "2"
        }
        cell {
          rowSpan = 3
          columnSpan = 3
          "3"
        }
        cell("1")
      }
      row("1")
      row("1", "1", "1")
      row("1", "1", "1", "1", "1", "1")
    }

    assertThat(table.render()).isEqualTo("""
      |3  111
      |   2 1
      |     1
      |2 3  1
      |     1
      |11   1
      |111111
      |""".trimMargin())
  }
}

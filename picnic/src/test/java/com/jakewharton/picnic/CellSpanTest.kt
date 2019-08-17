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

  @Test fun columnSpanAcrossDifferentSizedColumnsDoesNotExpand() {
    val table = table {
      row("1", "22", "333")
      row {
        cell {
          columnSpan = 3
          "666666"
        }
      }
    }

    assertThat(table.renderText()).isEqualTo("""
      |122333
      |666666
      |""".trimMargin())
  }

  @Test fun columnSpanAcrossBorderDoesNotExpand() {
    val table = table {
      row {
        cell {
          borderRight = true
          "11"
        }
        cell("22")
      }
      row {
        cell {
          columnSpan = 2
          "33333"
        }
      }
    }

    assertThat(table.renderText()).isEqualTo("""
      |11│22
      |33333
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

  @Test fun rowSpanAcrossDifferentSizedRowsDoesNotExpand() {
    val table = table {
      row {
        cell {
          rowSpan = 3
          "6\n6\n6\n6\n6\n6"
        }
        cell("1")
      }
      row("2\n2")
      row("3\n3\n3")
    }

    assertThat(table.renderText()).isEqualTo("""
      |61
      |62
      |62
      |63
      |63
      |63
      |""".trimMargin())
  }

  @Test fun rowSpanAcrossBorderDoesNotExpand() {
    val table = table {
      row {
        cell {
          rowSpan = 2
          "1\n1\n1"
        }
        cell {
          borderBottom = true

          "2"
        }
      }
      row("3")
    }

    assertThat(table.renderText()).isEqualTo("""
      |12
      |1─
      |13
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

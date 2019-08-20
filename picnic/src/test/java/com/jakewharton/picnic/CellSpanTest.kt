package com.jakewharton.picnic

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CellSpanTest {
  @Test fun columnSpans() {
    val table = table {
      row {
        cell("88888888") {
          columnSpan = 8
        }
        cell("1")
      }
      row {
        repeat(2) {
          cell("4444") {
            columnSpan = 4
          }
        }
        cell("1")
      }
      row {
        repeat(4) {
          cell("22") {
            columnSpan = 2
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
        cell("666666") {
          columnSpan = 3
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
        cell("11") {
          borderRight = true
        }
        cell("22")
      }
      row {
        cell("33333") {
          columnSpan = 2
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
        cell("8\n8\n8\n8\n8\n8\n8\n8") {
          rowSpan = 8
        }
        cell("4\n4\n4\n4") {
          rowSpan = 4
        }
        cell("2\n2") {
          rowSpan = 2
        }
        cell("1")
      }
      row("1")
      row {
        cell("2\n2") {
          rowSpan = 2
        }
        cell("1")
      }
      row("1")
      row {
        cell("4\n4\n4\n4") {
          rowSpan = 4
        }
        cell("2\n2") {
          rowSpan = 2
        }
        cell("1")
      }
      row("1")
      row {
        cell("2\n2") {
          rowSpan = 2
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
        cell("6\n6\n6\n6\n6\n6") {
          rowSpan = 3
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
        cell("1\n1\n1") {
          rowSpan = 2
        }
        cell("2") {
          borderBottom = true
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
        cell("333\n333\n333") {
          rowSpan = 3
          columnSpan = 3
        }
        cell("1")
        cell("1")
        cell("1")
      }
      row {
        cell("22\n22") {
          rowSpan = 2
          columnSpan = 2
        }
        cell("1")
      }
      row("1")
      row {
        cell("22\n22") {
          rowSpan = 2
          columnSpan = 2
        }
        cell("333\n333\n333") {
          rowSpan = 3
          columnSpan = 3
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

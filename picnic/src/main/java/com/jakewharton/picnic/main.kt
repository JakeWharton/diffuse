package com.jakewharton.picnic

fun main() {
  println(table {
    header {
      row {
        cell("Hey")
      }
    }
    body {
      row("One", "Two", "Three")
      row {
        cell {
          columnSpan = 2

          "Two columns!"
        }
        cell("One column")
      }
      row("Four", "Five", "Six")
    }
  })
}

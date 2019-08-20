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
        cell("Two columns!") {
          columnSpan = 2
          border = true
        }
        cell("One column")
      }
      row("Four", "Five", "Six")
    }
  })
}

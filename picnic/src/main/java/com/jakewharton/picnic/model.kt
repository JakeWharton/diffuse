package com.jakewharton.picnic

data class Table(val header: Header, val body: Body, val footer: Footer) {
  override fun toString() = renderText()
}

interface TableSection {
  val rows: List<Row>
}
data class Header(override val rows: List<Row>) : TableSection
data class Body(override val rows: List<Row>): TableSection
data class Footer(override val rows: List<Row>): TableSection

data class Row(val cells: List<Cell>)

data class Cell(val content: String, val columnSpan: Int = 1, val rowSpan: Int = 1)

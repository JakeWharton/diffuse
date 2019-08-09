package com.jakewharton.diffuse

import de.vandermeer.asciitable.AsciiTable
import de.vandermeer.asciitable.CWC_LongestLine
import de.vandermeer.asciithemes.TA_GridThemes

internal fun asciiTable(body: AsciiTable.() -> Unit): String = AsciiTable()
    .apply {
      renderer.cwc = CWC_LongestLine()
      context.gridTheme = TA_GridThemes.INSIDE.get()
    }
    .apply(body)
    .render()

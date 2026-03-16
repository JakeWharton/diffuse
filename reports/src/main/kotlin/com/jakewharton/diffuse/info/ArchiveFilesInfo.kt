package com.jakewharton.diffuse.info

import com.jakewharton.diffuse.diffuseTable
import com.jakewharton.diffuse.format.ArchiveFile
import com.jakewharton.diffuse.format.ArchiveFiles
import com.jakewharton.picnic.TableSectionDsl
import com.jakewharton.picnic.TextAlignment
import com.jakewharton.picnic.renderText
import me.saket.bytesize.binaryBytes

internal fun ArchiveFiles.toSummaryTable(
  name: String,
  displayTypes: List<ArchiveFile.Type>,
  skipIfEmptyTypes: Set<ArchiveFile.Type> = emptySet(),
  includeCompressed: Boolean = true,
) =
  diffuseTable {
      header {
        if (includeCompressed) {
          row {
            cell(name) { alignment = TextAlignment.BottomLeft }
            cell("compressed") { alignment = TextAlignment.BottomCenter }
            cell("uncompressed") { alignment = TextAlignment.BottomCenter }
          }
        } else {
          row {
            cell(name) { alignment = TextAlignment.BottomLeft }
            cell("size")
          }
        }
      }

      fun TableSectionDsl.addApkRow(name: String, type: ArchiveFile.Type? = null) {
        val old = if (type != null) filterValues { it.type == type } else this@toSummaryTable
        val oldSize = old.values.fold(0.binaryBytes) { acc, file -> acc + file.size }
        val oldUncompressedSize =
          old.values.fold(0.binaryBytes) { acc, file -> acc + file.uncompressedSize }
        if (oldSize != 0.binaryBytes || type !in skipIfEmptyTypes) {
          if (includeCompressed) {
            row(name, oldSize, oldUncompressedSize)
          } else {
            row(name, oldUncompressedSize)
          }
        }
      }

      body {
        cellStyle { alignment = TextAlignment.MiddleRight }
        for (type in displayTypes) {
          addApkRow(type.displayName, type)
        }
      }

      footer {
        cellStyle { alignment = TextAlignment.MiddleRight }
        addApkRow("total")
      }
    }
    .renderText()

package com.jakewharton.diffuse

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.diffuse.diff.ArchiveFilesDiff
import com.jakewharton.diffuse.diff.toSummaryTable
import com.jakewharton.diffuse.format.ArchiveFile
import com.jakewharton.diffuse.format.ArchiveFile.Type
import com.jakewharton.diffuse.format.ArchiveFiles
import me.saket.bytesize.binaryBytes
import org.junit.Test

class ArchiveFilesDiffTest {
  @Test
  fun summaryTableWithDirType() {
    val base =
      mapOf(
        "com/example/Main.class" to
          ArchiveFile(
            "com/example/Main.class",
            Type.Class,
            100.binaryBytes,
            200.binaryBytes,
            true,
          ),
        "META-INF/MANIFEST.MF" to
          ArchiveFile("META-INF/MANIFEST.MF", Type.Other, 30.binaryBytes, 40.binaryBytes, true),
      )
    val oldFiles =
      ArchiveFiles(
        base +
          mapOf(
            "com/example/" to
              ArchiveFile("com/example/", Type.Dir, 50.binaryBytes, 0.binaryBytes, true)
          )
      )
    val newFiles = ArchiveFiles(base)
    val diff = ArchiveFilesDiff(oldFiles, newFiles)
    val table = diff.toSummaryTable("JAR", Type.JAR_TYPES)
    assertThat(table)
      .isEqualTo(
        """
        |       │      compressed       │     uncompressed     
        |       ├───────┬───────┬───────┼───────┬───────┬──────
        | JAR   │ old   │ new   │ diff  │ old   │ new   │ diff 
        |───────┼───────┼───────┼───────┼───────┼───────┼──────
        | class │ 100 B │ 100 B │   0 B │ 200 B │ 200 B │  0 B 
        |   dir │  50 B │   0 B │ -50 B │   0 B │   0 B │  0 B 
        | other │  30 B │  30 B │   0 B │  40 B │  40 B │  0 B 
        |───────┼───────┼───────┼───────┼───────┼───────┼──────
        | total │ 180 B │ 130 B │ -50 B │ 240 B │ 240 B │  0 B 
        """
          .trimMargin()
      )
  }

  @Test
  fun summaryTableSkipsEmptyDirType() {
    val base =
      mapOf(
        "com/example/Main.class" to
          ArchiveFile(
            "com/example/Main.class",
            Type.Class,
            100.binaryBytes,
            200.binaryBytes,
            true,
          ),
        "META-INF/MANIFEST.MF" to
          ArchiveFile("META-INF/MANIFEST.MF", Type.Other, 30.binaryBytes, 40.binaryBytes, true),
      )
    val oldFiles = ArchiveFiles(base)
    val newFiles = ArchiveFiles(base)
    val diff = ArchiveFilesDiff(oldFiles, newFiles)
    val table = diff.toSummaryTable("JAR", Type.JAR_TYPES)
    assertThat(table)
      .isEqualTo(
        """
        |       │      compressed      │     uncompressed     
        |       ├───────┬───────┬──────┼───────┬───────┬──────
        | JAR   │ old   │ new   │ diff │ old   │ new   │ diff 
        |───────┼───────┼───────┼──────┼───────┼───────┼──────
        | class │ 100 B │ 100 B │  0 B │ 200 B │ 200 B │  0 B 
        | other │  30 B │  30 B │  0 B │  40 B │  40 B │  0 B 
        |───────┼───────┼───────┼──────┼───────┼───────┼──────
        | total │ 130 B │ 130 B │  0 B │ 240 B │ 240 B │  0 B 
        """
          .trimMargin()
      )
  }
}

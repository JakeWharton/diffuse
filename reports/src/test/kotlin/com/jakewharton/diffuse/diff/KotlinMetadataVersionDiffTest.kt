package com.jakewharton.diffuse.diff

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.jakewharton.diffuse.format.ApiMapping
import com.jakewharton.diffuse.format.Jar.Companion.toJar
import com.jakewharton.diffuse.format.TypeDescriptor
import com.jakewharton.diffuse.io.Input.Companion.asInput
import com.jakewharton.diffuse.testing.requireResource
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import okio.ByteString.Companion.toByteString
import org.junit.Test

class KotlinMetadataVersionDiffTest {
  @Test
  fun nonKotlinClassesAreIgnored() {
    val classBytes = Test::class.java.requireResource("Test.class").readBytes()
    val jarBytes = ByteArrayOutputStream()
    ZipOutputStream(jarBytes).use { zip ->
      zip.putNextEntry(ZipEntry("org/junit/Test.class"))
      zip.write(classBytes)
      zip.closeEntry()
    }
    val inputJar = jarBytes.toByteArray().toByteString().asInput("input.jar").toJar()

    val diff = JarsDiff(listOf(inputJar), ApiMapping.EMPTY, listOf(inputJar), ApiMapping.EMPTY)

    assertThat(diff.kotlinMetadataVersions.versionCounts).isEqualTo(emptyMap())
    assertThat(diff.kotlinMetadataVersions.changedItems).isEqualTo(emptyList())
  }

  @Test
  fun nothingChangedProducesNoOutput() {
    val diff =
      VersionDiff<TypeDescriptor, KotlinMetadataVersion>(
        versionCounts = emptyMap(),
        changedItems = emptyList(),
      )
    assertThat(diff.changed).isFalse()
    assertThat(buildString { appendVersionDiff("KOTLIN METADATA VERSIONS", diff) }).isEqualTo("")
  }

  @Test
  fun singleVersionUpgrade() {
    // One class moved from version 2.3.0 to 2.4.0.
    val diff =
      VersionDiff(
        versionCounts =
          mapOf(
            KotlinMetadataVersion(2, 3, 0) to (1 to 0), // -1 net
            KotlinMetadataVersion(2, 4, 0) to (0 to 1), // +1 net
          ),
        changedItems =
          listOf(
            Triple(
              TypeDescriptor("Lorg/example/MainKt;"),
              KotlinMetadataVersion(2, 3, 0),
              KotlinMetadataVersion(2, 4, 0),
            ) // Up
          ),
      )

    assertThat(diff.changed).isTrue()
    assertThat(buildString { appendVersionDiff("KOTLIN METADATA VERSIONS", diff) })
      .isEqualTo(
        """
        |
        |KOTLIN METADATA VERSIONS:
        |
        |   version │ old │ new │ diff       
        |  ─────────┼─────┼─────┼────────────
        |     2.3.0 │   1 │   0 │ -1 (+0 -1) 
        |     2.4.0 │   0 │   1 │ +1 (+1 -0) 
        |
        |  org.example.MainKt: 2.3.0 → 2.4.0
        |"""
          .trimMargin()
      )
  }

  @Test
  fun multipleClassesUpgrade() {
    // Two classes moved from 1.9.0 to 2.0.0; the unchanged-count version is filtered out.
    val diff =
      VersionDiff(
        versionCounts =
          mapOf(
            KotlinMetadataVersion(1, 9, 0) to (3 to 1), // -2 net
            KotlinMetadataVersion(2, 0, 0) to (0 to 2), // +2 net
          ),
        changedItems =
          listOf(
            Triple(
              TypeDescriptor("Lcom/example/ClassA;"),
              KotlinMetadataVersion(1, 9, 0),
              KotlinMetadataVersion(2, 0, 0),
            ), // Up
            Triple(
              TypeDescriptor("Lcom/example/ClassB;"),
              KotlinMetadataVersion(1, 9, 0),
              KotlinMetadataVersion(2, 0, 0),
            ), // Up
          ),
      )

    assertThat(buildString { appendVersionDiff("KOTLIN METADATA VERSIONS", diff) })
      .isEqualTo(
        """
        |
        |KOTLIN METADATA VERSIONS:
        |
        |   version │ old │ new │ diff       
        |  ─────────┼─────┼─────┼────────────
        |     1.9.0 │   3 │   1 │ -2 (+0 -2) 
        |     2.0.0 │   0 │   2 │ +2 (+2 -0) 
        |
        |  com.example.ClassA: 1.9.0 → 2.0.0
        |  com.example.ClassB: 1.9.0 → 2.0.0
        |"""
          .trimMargin()
      )
  }

  @Test
  fun singleVersionDowngrade() {
    // One class moved from version 2.4.0 back down to 2.3.0.
    val diff =
      VersionDiff(
        versionCounts =
          mapOf(
            KotlinMetadataVersion(2, 3, 0) to (0 to 1), // +1 net
            KotlinMetadataVersion(2, 4, 0) to (1 to 0), // -1 net
          ),
        changedItems =
          listOf(
            Triple(
              TypeDescriptor("Lorg/example/MainKt;"),
              KotlinMetadataVersion(2, 4, 0),
              KotlinMetadataVersion(2, 3, 0),
            ) // Down
          ),
      )

    assertThat(diff.changed).isTrue()
    assertThat(buildString { appendVersionDiff("KOTLIN METADATA VERSIONS", diff) })
      .isEqualTo(
        """
        |
        |KOTLIN METADATA VERSIONS:
        |
        |   version │ old │ new │ diff       
        |  ─────────┼─────┼─────┼────────────
        |     2.3.0 │   0 │   1 │ +1 (+1 -0) 
        |     2.4.0 │   1 │   0 │ -1 (+0 -1) 
        |
        |  org.example.MainKt: 2.4.0 → 2.3.0
        |"""
          .trimMargin()
      )
  }

  @Test
  fun multipleClassesDowngrade() {
    // Two classes moved from 2.0.0 down to 1.9.0.
    val diff =
      VersionDiff(
        versionCounts =
          mapOf(
            KotlinMetadataVersion(1, 9, 0) to (0 to 2), // +2 net
            KotlinMetadataVersion(2, 0, 0) to (2 to 0), // -2 net
          ),
        changedItems =
          listOf(
            Triple(
              TypeDescriptor("Lcom/example/ClassA;"),
              KotlinMetadataVersion(2, 0, 0),
              KotlinMetadataVersion(1, 9, 0),
            ), // Down
            Triple(
              TypeDescriptor("Lcom/example/ClassB;"),
              KotlinMetadataVersion(2, 0, 0),
              KotlinMetadataVersion(1, 9, 0),
            ), // Down
          ),
      )

    assertThat(buildString { appendVersionDiff("KOTLIN METADATA VERSIONS", diff) })
      .isEqualTo(
        """
        |
        |KOTLIN METADATA VERSIONS:
        |
        |   version │ old │ new │ diff       
        |  ─────────┼─────┼─────┼────────────
        |     1.9.0 │   0 │   2 │ +2 (+2 -0) 
        |     2.0.0 │   2 │   0 │ -2 (+0 -2) 
        |
        |  com.example.ClassA: 2.0.0 → 1.9.0
        |  com.example.ClassB: 2.0.0 → 1.9.0
        |"""
          .trimMargin()
      )
  }

  @Test
  fun mixedUpgradeAndDowngrade() {
    val diff =
      VersionDiff(
        versionCounts =
          mapOf(
            KotlinMetadataVersion(2, 0, 0) to (2 to 1), // -1 net
            KotlinMetadataVersion(2, 4, 0) to (0 to 1), // +1 net
            // Version 1.9.0 is old 1, new 1 (Filtered)
          ),
        changedItems =
          listOf(
            Triple(
              TypeDescriptor("Lcom/example/ClassA;"),
              KotlinMetadataVersion(1, 9, 0),
              KotlinMetadataVersion(2, 0, 0),
            ), // Up
            Triple(
              TypeDescriptor("Lcom/example/ClassB;"),
              KotlinMetadataVersion(2, 0, 0),
              KotlinMetadataVersion(1, 9, 0),
            ), // Down
            Triple(
              TypeDescriptor("Lcom/example/ClassC;"),
              KotlinMetadataVersion(2, 0, 0),
              KotlinMetadataVersion(2, 4, 0),
            ), // Up
          ),
      )

    assertThat(buildString { appendVersionDiff("KOTLIN METADATA VERSIONS", diff) })
      .isEqualTo(
        """
        |
        |KOTLIN METADATA VERSIONS:
        |
        |   version │ old │ new │ diff       
        |  ─────────┼─────┼─────┼────────────
        |     2.0.0 │   2 │   1 │ -1 (+0 -1) 
        |     2.4.0 │   0 │   1 │ +1 (+1 -0) 
        |
        |  com.example.ClassA: 1.9.0 → 2.0.0
        |  com.example.ClassB: 2.0.0 → 1.9.0
        |  com.example.ClassC: 2.0.0 → 2.4.0
        |"""
          .trimMargin()
      )
  }

  @Test
  fun noChangedClassesButVersionCountsDiffer() {
    // New classes added at version 2.0.0, no pre-existing classes changed version.
    val diff =
      VersionDiff<TypeDescriptor, KotlinMetadataVersion>(
        versionCounts =
          mapOf(
            KotlinMetadataVersion(2, 0, 0) to (0 to 2) // +2 net
          ),
        changedItems = emptyList(),
      )

    assertThat(buildString { appendVersionDiff("KOTLIN METADATA VERSIONS", diff) })
      .isEqualTo(
        """
        |
        |KOTLIN METADATA VERSIONS:
        |
        |   version │ old │ new │ diff       
        |  ─────────┼─────┼─────┼────────────
        |     2.0.0 │   0 │   2 │ +2 (+2 -0) 
        |"""
          .trimMargin()
      )
  }

  @Test
  fun onlyRemovals() {
    // Classes removed at version 1.9.0.
    val diff =
      VersionDiff<TypeDescriptor, KotlinMetadataVersion>(
        versionCounts =
          mapOf(
            KotlinMetadataVersion(1, 9, 0) to (2 to 0) // -2 net
          ),
        changedItems = emptyList(),
      )

    assertThat(buildString { appendVersionDiff("KOTLIN METADATA VERSIONS", diff) })
      .isEqualTo(
        """
        |
        |KOTLIN METADATA VERSIONS:
        |
        |   version │ old │ new │ diff       
        |  ─────────┼─────┼─────┼────────────
        |     1.9.0 │   2 │   0 │ -2 (+0 -2) 
        |"""
          .trimMargin()
      )
  }

  @Test
  fun netZeroVersionChangesStillProducesOutput() {
    // Classes shifted versions but net counts per version stayed same.
    // Table is hidden, but class list is shown.
    val diff =
      VersionDiff(
        versionCounts = emptyMap(),
        changedItems =
          listOf(
            Triple(
              TypeDescriptor("Lcom/example/ClassA;"),
              KotlinMetadataVersion(1, 9, 0),
              KotlinMetadataVersion(2, 0, 0),
            ), // Up
            Triple(
              TypeDescriptor("Lcom/example/ClassB;"),
              KotlinMetadataVersion(2, 0, 0),
              KotlinMetadataVersion(1, 9, 0),
            ), // Down
          ),
      )

    assertThat(diff.changed).isTrue()
    assertThat(buildString { appendVersionDiff("KOTLIN METADATA VERSIONS", diff) })
      .isEqualTo(
        """
        |
        |KOTLIN METADATA VERSIONS:
        |
        |  com.example.ClassA: 1.9.0 → 2.0.0
        |  com.example.ClassB: 2.0.0 → 1.9.0
        |"""
          .trimMargin()
      )
  }
}

private fun KotlinMetadataVersion(vararg numbers: Int) = KotlinMetadataVersion(numbers.toList())

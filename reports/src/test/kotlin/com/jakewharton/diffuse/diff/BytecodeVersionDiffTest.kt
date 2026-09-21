package com.jakewharton.diffuse.diff

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.jakewharton.diffuse.format.TypeDescriptor
import org.junit.Test

class BytecodeVersionDiffTest {
  @Test
  fun nothingChangedProducesNoOutput() {
    val diff =
      VersionDiff<TypeDescriptor, Int>(versionCounts = emptyMap(), changedItems = emptyList())
    assertThat(diff.changed).isFalse()
    assertThat(buildString { appendVersionDiff("BYTECODE VERSIONS", diff) }).isEqualTo("")
  }

  @Test
  fun singleVersionUpgrade() {
    // One class moved from version 65 to 69.
    val diff =
      VersionDiff(
        versionCounts =
          mapOf(
            65 to (1 to 0), // -1 net
            69 to (0 to 1), // +1 net
          ),
        changedItems =
          listOf(
            Triple(
              TypeDescriptor("Lorg/example/MainKt;"),
              65,
              69,
            ) // Up
          ),
      )

    assertThat(diff.changed).isTrue()
    assertThat(buildString { appendVersionDiff("BYTECODE VERSIONS", diff) })
      .isEqualTo(
        """
        |
        |BYTECODE VERSIONS:
        |
        |   version │ old │ new │ diff       
        |  ─────────┼─────┼─────┼────────────
        |        65 │   1 │   0 │ -1 (+0 -1) 
        |        69 │   0 │   1 │ +1 (+1 -0) 
        |
        |  org.example.MainKt: 65 → 69
        |"""
          .trimMargin()
      )
  }

  @Test
  fun multipleClassesUpgrade() {
    // Two classes moved from 61 to 65; the unchanged-count version is filtered out.
    val diff =
      VersionDiff(
        versionCounts =
          mapOf(
            61 to (3 to 1), // -2 net
            65 to (0 to 2), // +2 net
          ),
        changedItems =
          listOf(
            Triple(
              TypeDescriptor("Lcom/example/ClassA;"),
              61,
              65,
            ), // Up
            Triple(
              TypeDescriptor("Lcom/example/ClassB;"),
              61,
              65,
            ), // Up
          ),
      )

    assertThat(buildString { appendVersionDiff("BYTECODE VERSIONS", diff) })
      .isEqualTo(
        """
        |
        |BYTECODE VERSIONS:
        |
        |   version │ old │ new │ diff       
        |  ─────────┼─────┼─────┼────────────
        |        61 │   3 │   1 │ -2 (+0 -2) 
        |        65 │   0 │   2 │ +2 (+2 -0) 
        |
        |  com.example.ClassA: 61 → 65
        |  com.example.ClassB: 61 → 65
        |"""
          .trimMargin()
      )
  }

  @Test
  fun singleVersionDowngrade() {
    // One class moved from version 69 back down to 65.
    val diff =
      VersionDiff(
        versionCounts =
          mapOf(
            65 to (0 to 1), // +1 net
            69 to (1 to 0), // -1 net
          ),
        changedItems =
          listOf(
            Triple(
              TypeDescriptor("Lorg/example/MainKt;"),
              69,
              65,
            ) // Down
          ),
      )

    assertThat(diff.changed).isTrue()
    assertThat(buildString { appendVersionDiff("BYTECODE VERSIONS", diff) })
      .isEqualTo(
        """
        |
        |BYTECODE VERSIONS:
        |
        |   version │ old │ new │ diff       
        |  ─────────┼─────┼─────┼────────────
        |        65 │   0 │   1 │ +1 (+1 -0) 
        |        69 │   1 │   0 │ -1 (+0 -1) 
        |
        |  org.example.MainKt: 69 → 65
        |"""
          .trimMargin()
      )
  }

  @Test
  fun multipleClassesDowngrade() {
    // Two classes moved from 65 down to 61.
    val diff =
      VersionDiff(
        versionCounts =
          mapOf(
            61 to (0 to 2), // +2 net
            65 to (2 to 0), // -2 net
          ),
        changedItems =
          listOf(
            Triple(
              TypeDescriptor("Lcom/example/ClassA;"),
              65,
              61,
            ), // Down
            Triple(
              TypeDescriptor("Lcom/example/ClassB;"),
              65,
              61,
            ), // Down
          ),
      )

    assertThat(buildString { appendVersionDiff("BYTECODE VERSIONS", diff) })
      .isEqualTo(
        """
        |
        |BYTECODE VERSIONS:
        |
        |   version │ old │ new │ diff       
        |  ─────────┼─────┼─────┼────────────
        |        61 │   0 │   2 │ +2 (+2 -0) 
        |        65 │   2 │   0 │ -2 (+0 -2) 
        |
        |  com.example.ClassA: 65 → 61
        |  com.example.ClassB: 65 → 61
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
            65 to (2 to 1), // -1 net
            69 to (0 to 1), // +1 net
            // Version 61 is old 1, new 1 (Filtered)
          ),
        changedItems =
          listOf(
            Triple(
              TypeDescriptor("Lcom/example/ClassA;"),
              61,
              65,
            ), // Up
            Triple(
              TypeDescriptor("Lcom/example/ClassB;"),
              65,
              61,
            ), // Down
            Triple(
              TypeDescriptor("Lcom/example/ClassC;"),
              65,
              69,
            ), // Up
          ),
      )

    assertThat(buildString { appendVersionDiff("BYTECODE VERSIONS", diff) })
      .isEqualTo(
        """
        |
        |BYTECODE VERSIONS:
        |
        |   version │ old │ new │ diff       
        |  ─────────┼─────┼─────┼────────────
        |        65 │   2 │   1 │ -1 (+0 -1) 
        |        69 │   0 │   1 │ +1 (+1 -0) 
        |
        |  com.example.ClassA: 61 → 65
        |  com.example.ClassB: 65 → 61
        |  com.example.ClassC: 65 → 69
        |"""
          .trimMargin()
      )
  }

  @Test
  fun noChangedClassesButVersionCountsDiffer() {
    // New classes added at version 65, no pre-existing classes changed version.
    val diff =
      VersionDiff<TypeDescriptor, Int>(
        versionCounts =
          mapOf(
            65 to (0 to 2) // +2 net
          ),
        changedItems = emptyList(),
      )

    assertThat(buildString { appendVersionDiff("BYTECODE VERSIONS", diff) })
      .isEqualTo(
        """
        |
        |BYTECODE VERSIONS:
        |
        |   version │ old │ new │ diff       
        |  ─────────┼─────┼─────┼────────────
        |        65 │   0 │   2 │ +2 (+2 -0) 
        |"""
          .trimMargin()
      )
  }

  @Test
  fun onlyRemovals() {
    // Classes removed at version 61.
    val diff =
      VersionDiff<TypeDescriptor, Int>(
        versionCounts =
          mapOf(
            61 to (2 to 0) // -2 net
          ),
        changedItems = emptyList(),
      )

    assertThat(buildString { appendVersionDiff("BYTECODE VERSIONS", diff) })
      .isEqualTo(
        """
        |
        |BYTECODE VERSIONS:
        |
        |   version │ old │ new │ diff       
        |  ─────────┼─────┼─────┼────────────
        |        61 │   2 │   0 │ -2 (+0 -2) 
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
              61,
              65,
            ), // Up
            Triple(
              TypeDescriptor("Lcom/example/ClassB;"),
              65,
              61,
            ), // Down
          ),
      )

    assertThat(diff.changed).isTrue()
    assertThat(buildString { appendVersionDiff("BYTECODE VERSIONS", diff) })
      .isEqualTo(
        """
        |
        |BYTECODE VERSIONS:
        |
        |  com.example.ClassA: 61 → 65
        |  com.example.ClassB: 65 → 61
        |"""
          .trimMargin()
      )
  }
}

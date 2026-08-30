package com.jakewharton.diffuse.diff

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.containsOnly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.jakewharton.diffuse.format.TypeDescriptor
import org.junit.Test

class VersionDiffTest {
  @Test
  fun nothingChanged() {
    val items =
      listOf(
        mapOf(
          TypeDescriptor("Lcom/example/ClassA;") to 61,
          TypeDescriptor("Lcom/example/ClassB;") to 65,
        )
      )

    val diff = versionDiff(items, items) { it }

    assertThat(diff.changed).isFalse()
    assertThat(diff.versionCounts).isEmpty()
    assertThat(diff.changedItems).isEmpty()
  }

  @Test
  fun unchangedVersionsAreFilteredOutFromVersionCounts() {
    val oldItems =
      listOf(
        mapOf(
          TypeDescriptor("Lcom/example/ClassA;") to 61,
          TypeDescriptor("Lcom/example/ClassB;") to 65,
          TypeDescriptor("Lcom/example/ClassUnchanged;") to 61,
        )
      )
    val newItems =
      listOf(
        mapOf(
          TypeDescriptor("Lcom/example/ClassA;") to 65,
          TypeDescriptor("Lcom/example/ClassB;") to 61,
          TypeDescriptor("Lcom/example/ClassUnchanged;") to 61,
        )
      )

    val diff = versionDiff(oldItems, newItems) { it }

    assertThat(diff.changed).isTrue()
    // 61: old=2, new=2 (net=0 -> filtered)
    // 65: old=1, new=1 (net=0 -> filtered)
    assertThat(diff.versionCounts).isEmpty()
    assertThat(diff.changedItems)
      .containsExactly(
        Triple(TypeDescriptor("Lcom/example/ClassA;"), 61, 65),
        Triple(TypeDescriptor("Lcom/example/ClassB;"), 65, 61),
      )
  }

  @Test
  fun partialVersionCountChangeFiltersOnlyUnchanged() {
    val oldItems =
      listOf(
        mapOf(
          TypeDescriptor("Lcom/example/ClassA;") to 61,
          TypeDescriptor("Lcom/example/ClassB;") to 65,
          TypeDescriptor("Lcom/example/ClassC;") to 65,
        )
      )
    val newItems =
      listOf(
        mapOf(
          TypeDescriptor("Lcom/example/ClassA;") to 65,
          TypeDescriptor("Lcom/example/ClassB;") to 61,
          TypeDescriptor("Lcom/example/ClassC;") to 69,
        )
      )

    val diff = versionDiff(oldItems, newItems) { it }

    assertThat(diff.changed).isTrue()
    // 61: old=1, new=1 (net=0 -> filtered)
    // 65: old=2, new=1 (net=-1 -> kept)
    // 69: old=0, new=1 (net=+1 -> kept)
    assertThat(diff.versionCounts)
      .isEqualTo(
        mapOf(
          65 to (2 to 1),
          69 to (0 to 1),
        )
      )
    assertThat(diff.changedItems)
      .containsExactly(
        Triple(TypeDescriptor("Lcom/example/ClassA;"), 61, 65),
        Triple(TypeDescriptor("Lcom/example/ClassB;"), 65, 61),
        Triple(TypeDescriptor("Lcom/example/ClassC;"), 65, 69),
      )
  }

  @Test
  fun multipleDexesAggregation() {
    val oldItems =
      listOf(
        mapOf(
          "classes.dex" to 35,
          "classes2.dex" to 35,
        ),
        mapOf("classes3.dex" to 35),
      )
    val newItems =
      listOf(
        mapOf("classes.dex" to 35),
        mapOf(
          "classes2.dex" to 38,
          "classes3.dex" to 38,
        ),
      )

    val diff = versionDiff(oldItems, newItems) { it }

    assertThat(diff.changed).isTrue()
    assertThat(diff.versionCounts)
      .containsOnly(
        35 to (3 to 1),
        38 to (0 to 2),
      )
    assertThat(diff.changedItems)
      .containsExactly(
        Triple("classes2.dex", 35, 38),
        Triple("classes3.dex", 35, 38),
      )
  }
}

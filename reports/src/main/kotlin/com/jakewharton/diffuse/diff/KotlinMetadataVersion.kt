package com.jakewharton.diffuse.diff

internal data class KotlinMetadataVersion(private val numbers: List<Int>) :
  Comparable<KotlinMetadataVersion> {
  private val major = numbers[0]
  private val minor = numbers[1]
  private val patch = numbers[2]

  override fun compareTo(other: KotlinMetadataVersion): Int = comparator.compare(this, other)

  override fun toString(): String = numbers.joinToString(".")

  private companion object {
    val comparator =
      compareBy(
        KotlinMetadataVersion::major,
        KotlinMetadataVersion::minor,
        KotlinMetadataVersion::patch,
      )
  }
}

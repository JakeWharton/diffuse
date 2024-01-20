package com.jakewharton.diffuse

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.diffuse.diff.SignaturesDiff
import com.jakewharton.diffuse.diff.toDetailReport
import com.jakewharton.diffuse.format.Signatures
import okio.ByteString.Companion.encodeUtf8
import org.junit.Test

class SignaturesDiffTest {
  private val signatureEmpty = Signatures(emptyList(), emptyList(), emptyList(), emptyList())
  private val signatureV1Only = Signatures(listOf("v1v1".encodeUtf8()), emptyList(), emptyList(), emptyList())
  private val signatureV1AndV2 = Signatures(listOf("v1v1".encodeUtf8()), listOf("v2v2".encodeUtf8()), emptyList(), emptyList())
  private val signatureV1AndV3 = Signatures(listOf("v1v1".encodeUtf8()), emptyList(), listOf("v3v3".encodeUtf8()), emptyList())
  private val signatureV3AndV4 = Signatures(emptyList(), emptyList(), listOf("v3v3".encodeUtf8()), listOf("v4v4".encodeUtf8()))

  @Test fun emptyToV1() {
    val diff = SignaturesDiff(signatureEmpty, signatureV1Only)
    assertThat(diff.toDetailReport()).isEqualTo(
      """
      |
      | SIGNATURES │ old │ new      
      |────────────┼─────┼──────────
      |         V1 │     │ 76317631 
      |
      """.trimMargin(),
    )
  }

  @Test fun v1ToV1AndV2() {
    val diff = SignaturesDiff(signatureV1Only, signatureV1AndV2)
    assertThat(diff.toDetailReport()).isEqualTo(
      """
      |
      | SIGNATURES │ old      │ new      
      |────────────┼──────────┼──────────
      |         V1 │ 76317631 │ 76317631 
      |         V2 │          │ 76327632 
      |
      """.trimMargin(),
    )
  }

  @Test fun v1AndV2ToV1AndV3() {
    val diff = SignaturesDiff(signatureV1AndV2, signatureV1AndV3)
    assertThat(diff.toDetailReport()).isEqualTo(
      """
      |
      | SIGNATURES │ old      │ new      
      |────────────┼──────────┼──────────
      |         V1 │ 76317631 │ 76317631 
      |         V2 │ 76327632 │          
      |         V3 │          │ 76337633 
      |
      """.trimMargin(),
    )
  }

  @Test fun v1AndV2ToV3AndV4() {
    val diff = SignaturesDiff(signatureV1AndV2, signatureV3AndV4)
    assertThat(diff.toDetailReport()).isEqualTo(
      """
      |
      | SIGNATURES │ old      │ new      
      |────────────┼──────────┼──────────
      |         V1 │ 76317631 │          
      |         V2 │ 76327632 │          
      |         V3 │          │ 76337633 
      |         V4 │          │ 76347634 
      |
      """.trimMargin(),
    )
  }
}

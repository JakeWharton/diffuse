package com.jakewharton.diffuse

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.doesNotContain
import com.jakewharton.diffuse.diff.JarDiff
import com.jakewharton.diffuse.diff.JarManifestDiff.Companion.MANIFEST_PATH
import com.jakewharton.diffuse.format.ApiMapping
import com.jakewharton.diffuse.format.Jar
import com.jakewharton.diffuse.format.Jar.Companion.toJar
import com.jakewharton.diffuse.io.Input.Companion.asInput
import java.io.ByteArrayOutputStream
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry
import okio.ByteString.Companion.toByteString
import org.junit.Test

class JarDiffTextReportTest {
  @Test
  fun manifestChanged() {
    val oldJar = jar("old.jar", "Manifest-Version: 1.0\nCreated-By: old")
    val newJar = jar("new.jar", "Manifest-Version: 1.0\nCreated-By: new")

    val report =
      JarDiff(oldJar, ApiMapping.EMPTY, newJar, ApiMapping.EMPTY).toTextReport(false).toString()

    assertThat(report)
      .contains(
        """
        |======================
        |====   MANIFEST   ====
        |======================
        |
        |@@ -1,2 +1,2 @@
        | Manifest-Version: 1.0
        |-Created-By: old
        |+Created-By: new
        """
          .trimMargin()
      )
  }

  @Test
  fun manifestNotChanged() {
    val oldJar = jar("old.jar", "Manifest-Version: 1.0\nCreated-By: same")
    val newJar = jar("new.jar", "Manifest-Version: 1.0\nCreated-By: same")

    val report =
      JarDiff(oldJar, ApiMapping.EMPTY, newJar, ApiMapping.EMPTY).toTextReport(false).toString()

    assertThat(report).doesNotContain("====   MANIFEST   ====")
  }

  private fun jar(name: String, manifestContent: String): Jar {
    val bytes = ByteArrayOutputStream()
    JarOutputStream(bytes).use { jar ->
      jar.putNextEntry(ZipEntry(MANIFEST_PATH))
      jar.write(manifestContent.toByteArray())
      jar.closeEntry()
    }
    return bytes.toByteArray().toByteString().asInput(name).toJar()
  }
}

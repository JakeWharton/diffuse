package com.jakewharton.diffuse

import com.google.devrel.gmscore.tools.apk.arsc.BinaryResourceFile
import com.google.devrel.gmscore.tools.apk.arsc.XmlChunk
import com.google.devrel.gmscore.tools.apk.arsc.XmlStartElementChunk
import org.w3c.dom.Element
import org.xml.sax.InputSource
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory

class Manifest private constructor(
  val packageName: String,
  val versionName: String?,
  val versionCode: Long?
) {
  companion object {
    @JvmStatic
    @JvmName("parse")
    fun BinaryResourceFile.toManifest(): Manifest {
      val rootChunk = requireNotNull(chunks.singleOrNull() as XmlChunk?) {
        "Unable to parse manifest from binary XML"
      }

      var packageName: String? = null
      var versionName: String? = null
      var versionCodeMinor: Int? = null
      var versionCodeMajor = 0

      val manifestChunk = rootChunk.chunks.values
          .filterIsInstance<XmlStartElementChunk>()
          .singleOrNull { it.name == "manifest" }
          ?: throw IllegalArgumentException("Unable to find root <manifest> tag")

      for (attribute in manifestChunk.attributes) {
        when (attribute.name()) {
          "package" -> packageName = attribute.rawValue()!!
          "versionName" -> versionName = attribute.rawValue()!!
          "versionCode" -> versionCodeMinor = attribute.typedValue().data()
          "versionCodeMajor" -> versionCodeMajor = attribute.typedValue().data()
        }
      }

      val versionCode = (versionCodeMajor.toLong() shl 32) +
          requireNotNull(versionCodeMinor) { "<manifest> missing 'versionCode' attribute." }
      return Manifest(
          requireNotNull(packageName) { "<manifest> missing 'package' attribute." },
          requireNotNull(versionName) { "<manifest> missing 'versionName' attribute." },
          versionCode)
    }

    @JvmStatic
    @JvmName("parse")
    fun String.toManifest(): Manifest {
      val documentBuilderFactory = DocumentBuilderFactory.newInstance()
      documentBuilderFactory.isNamespaceAware = true
      val documentBuilder = documentBuilderFactory.newDocumentBuilder()
      val document = documentBuilder.parse(InputSource(StringReader(this)))
      val manifestElement = document.documentElement
      require(manifestElement.tagName == "manifest") {
        "Unable to find root <manifest> tag"
      }

      val packageName = manifestElement.getAttribute("package")
      val versionName = manifestElement.getAttributeOrNull(ANDROID_NS, "versionName")
      val versionCodeMinor = manifestElement.getAttributeOrNull(ANDROID_NS, "versionCode")?.toInt()
      val versionCodeMajor = manifestElement.getAttributeOrNull(ANDROID_NS, "versionCodeMajor")?.toInt() ?: 0
      val versionCode = if (versionCodeMinor != null) {
        (versionCodeMajor.toLong() shl 32) + versionCodeMinor
      } else {
        null
      }

      return Manifest(packageName, versionName, versionCode)
    }

    private fun Element.getAttributeOrNull(namespace: String?, name: String): String? {
      return if (hasAttributeNS(namespace, name)) {
        getAttributeNS(namespace, name)
      } else {
        null
      }
    }

    private const val ANDROID_NS = "http://schemas.android.com/apk/res/android"
  }
}

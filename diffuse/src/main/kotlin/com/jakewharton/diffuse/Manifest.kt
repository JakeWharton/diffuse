package com.jakewharton.diffuse

import com.google.devrel.gmscore.tools.apk.arsc.BinaryResourceFile
import com.google.devrel.gmscore.tools.apk.arsc.Chunk
import com.google.devrel.gmscore.tools.apk.arsc.XmlChunk
import com.google.devrel.gmscore.tools.apk.arsc.XmlEndElementChunk
import com.google.devrel.gmscore.tools.apk.arsc.XmlNamespaceStartChunk
import com.google.devrel.gmscore.tools.apk.arsc.XmlStartElementChunk
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.NamedNodeMap
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.InputSource
import java.io.StringReader
import java.util.ArrayDeque
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory

class Manifest private constructor(
  val xml: String,
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

      val document = DocumentBuilderFactory.newInstance()
          .apply {
            isNamespaceAware = true
          }
          .newDocumentBuilder()
          .newDocument()

      val nodeStack = ArrayDeque<Node>().apply { add(document) }
      val namespacesToAdd = mutableMapOf<String, String>()
      val namespacesInScope = mutableMapOf<String?, String>(null to "")
      fun Chunk.parseChunk() {
        when (this) {
          is XmlNamespaceStartChunk -> {
            check(namespacesToAdd.put(prefix, uri) == null)
            check(namespacesInScope.put(uri, "$prefix:") == null)
          }
          is XmlStartElementChunk -> {
            val canonicalNamespace = namespace.takeIf(String::isNotEmpty)
            val canonicalName = namespacesInScope[canonicalNamespace] + name
            val element = document.createElementNS(canonicalNamespace, canonicalName)
            if (namespacesToAdd.isNotEmpty()) {
              namespacesToAdd.forEach { (prefix, uri) ->
                element.setAttribute("xmlns:$prefix", uri)
              }
              namespacesToAdd.clear()
            }

            for (attribute in attributes) {
              val attributeNamespace = attribute.namespace().takeIf(String::isNotEmpty)
              val attributeName = namespacesInScope[attributeNamespace] + attribute.name()
              val attributeValue =
                if (attribute.rawValueIndex() != -1) attribute.rawValue()
                else attribute.typedValue().data().toString()
              element.setAttributeNS(attributeNamespace, attributeName, attributeValue)
            }
            nodeStack.peekFirst()!!.appendChild(element)
            nodeStack.addFirst(element)
          }
          is XmlEndElementChunk -> {
            nodeStack.removeFirst()
          }
        }
      }
      rootChunk.chunks.values.forEach(Chunk::parseChunk)

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
          document.toFormattedXml(),
          requireNotNull(packageName) { "<manifest> missing 'package' attribute." },
          requireNotNull(versionName) { "<manifest> missing 'versionName' attribute." },
          versionCode)
    }

    @JvmStatic
    @JvmName("parse")
    fun String.toManifest(): Manifest {
      val documentBuilderFactory = DocumentBuilderFactory.newInstance().apply {
        isNamespaceAware = true
      }
      val document = documentBuilderFactory.newDocumentBuilder()
          .parse(InputSource(StringReader(this)))
      document.superNormalize()

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

      return Manifest(document.toFormattedXml(), packageName, versionName, versionCode)
    }

    private fun Document.superNormalize() {
      normalize()

      val emptyNodes = XPathFactory.newInstance().newXPath().evaluate(
          "//text()[normalize-space()='']", this, XPathConstants.NODESET
      ) as NodeList
      for (emptyNode in emptyNodes) {
        emptyNode.parentNode.removeChild(emptyNode)
      }
    }

    private fun Document.toFormattedXml() = buildString {
      fun appendIndent(indent: Int) {
        repeat(indent) {
          append("  ")
        }
      }
      fun appendNode(node: Node, indent: Int) {
        appendIndent(indent)
        append('<')
        append(node.nodeName)
        if (node.hasAttributes()) {
          // TODO sort attributes
          //  xmlns: should be first
          //  otherwise alphabetical
          for (attribute in node.attributes) {
            appendln()
            appendIndent(indent + 2)
            append(attribute.nodeName)
            append("=\"")
            append(attribute.nodeValue)
            append('"')
          }
          appendln()
          appendIndent(indent + 2)
        }
        if (!node.hasChildNodes()) {
          append('/')
        }
        appendln('>')

        if (node.hasChildNodes()) {
          for (child in node.childNodes) {
            appendNode(child, indent + 1)
          }

          appendIndent(indent)
          append("</")
          append(node.nodeName)
          appendln('>')
        }
      }

      appendNode(documentElement, 0)
    }

    private operator fun NodeList.iterator() = object : Iterator<Node> {
      private var index = 0
      override fun hasNext() = index < length
      override fun next() = item(index++)
    }

    private operator fun NamedNodeMap.iterator() = object : Iterator<Node> {
      private var index = 0
      override fun hasNext() = index < length
      override fun next() = item(index++)
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

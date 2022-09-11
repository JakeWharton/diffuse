package com.jakewharton.diffuse.format

import com.android.aapt.Resources.XmlNode
import com.android.tools.build.bundletool.model.utils.xmlproto.XmlProtoNode
import com.android.tools.build.bundletool.xml.XmlProtoToXmlConverter
import com.google.devrel.gmscore.tools.apk.arsc.BinaryResourceFile
import com.google.devrel.gmscore.tools.apk.arsc.BinaryResourceValue.Type.INT_BOOLEAN
import com.google.devrel.gmscore.tools.apk.arsc.BinaryResourceValue.Type.INT_COLOR_ARGB4
import com.google.devrel.gmscore.tools.apk.arsc.BinaryResourceValue.Type.INT_COLOR_ARGB8
import com.google.devrel.gmscore.tools.apk.arsc.BinaryResourceValue.Type.INT_COLOR_RGB4
import com.google.devrel.gmscore.tools.apk.arsc.BinaryResourceValue.Type.INT_COLOR_RGB8
import com.google.devrel.gmscore.tools.apk.arsc.BinaryResourceValue.Type.INT_DEC
import com.google.devrel.gmscore.tools.apk.arsc.BinaryResourceValue.Type.INT_HEX
import com.google.devrel.gmscore.tools.apk.arsc.BinaryResourceValue.Type.NULL
import com.google.devrel.gmscore.tools.apk.arsc.BinaryResourceValue.Type.REFERENCE
import com.google.devrel.gmscore.tools.apk.arsc.BinaryResourceValue.Type.STRING
import com.google.devrel.gmscore.tools.apk.arsc.XmlChunk
import com.google.devrel.gmscore.tools.apk.arsc.XmlEndElementChunk
import com.google.devrel.gmscore.tools.apk.arsc.XmlNamespaceStartChunk
import com.google.devrel.gmscore.tools.apk.arsc.XmlStartElementChunk
import com.jakewharton.diffuse.io.Input
import java.io.StringReader
import java.util.ArrayDeque
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.NamedNodeMap
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.InputSource

class AndroidManifest private constructor(
  val xml: String,
  val packageName: String,
  val versionName: String?,
  val versionCode: Long?,
) {
  companion object {
    const val NAME = "AndroidManifest.xml"

    private val documentBuilderFactory = DocumentBuilderFactory.newInstance()!!
      .apply {
        isNamespaceAware = true
      }

    internal fun BinaryResourceFile.toManifest(arsc: Arsc? = null): AndroidManifest {
      return toDocument(arsc).toManifest()
    }

    @JvmStatic
    @JvmName("parse")
    fun String.toManifest(): AndroidManifest = toDocument().toManifest()

    @JvmStatic
    @JvmName("parse")
    fun Input.toManifest(): AndroidManifest = toUtf8().toManifest()

    internal fun XmlNode.toManifest(): AndroidManifest {
      return XmlProtoToXmlConverter.convert(XmlProtoNode(this))
        .apply { normalizeWhitespace() }
        .toManifest()
    }

    private fun BinaryResourceFile.toDocument(arsc: Arsc?): Document {
      val rootChunk = requireNotNull(chunks.singleOrNull() as XmlChunk?) {
        "Unable to parse manifest from binary XML"
      }

      val document = documentBuilderFactory
        .newDocumentBuilder()
        .newDocument()

      val nodeStack = ArrayDeque<Node>().apply { add(document) }
      val namespacesToAdd = mutableMapOf<String, String>()
      val namespacesInScope = mutableMapOf<String?, String>(null to "")
      rootChunk.chunks.values.forEach { chunk ->
        when (chunk) {
          is XmlNamespaceStartChunk -> {
            check(namespacesToAdd.put(chunk.prefix, chunk.uri) == null)
            val newPrefix = "${chunk.prefix}:"
            val oldPrefix = namespacesInScope.put(chunk.uri, newPrefix)
            check(oldPrefix == null || oldPrefix == newPrefix)
          }
          is XmlStartElementChunk -> {
            val canonicalNamespace = chunk.namespace.takeIf(String::isNotEmpty)
            val canonicalName = namespacesInScope[canonicalNamespace] + chunk.name
            val element = document.createElementNS(canonicalNamespace, canonicalName)
            if (namespacesToAdd.isNotEmpty()) {
              namespacesToAdd.forEach { (prefix, uri) ->
                element.setAttribute("xmlns:$prefix", uri)
              }
              namespacesToAdd.clear()
            }

            for (attribute in chunk.attributes) {
              val attributeNamespace = attribute.namespace().takeIf(String::isNotEmpty)
              val attributeName = namespacesInScope[attributeNamespace] + attribute.name()

              val typedValue = attribute.typedValue()
              val attributeValue = when (typedValue.type()) {
                INT_BOOLEAN -> if (typedValue.data() == 0) "false" else "true"
                INT_COLOR_ARGB4 -> String.format("#%04x", typedValue.data())
                INT_COLOR_ARGB8 -> String.format("#%08x", typedValue.data())
                INT_COLOR_RGB4 -> String.format("#%03x", typedValue.data())
                INT_COLOR_RGB8 -> String.format("#%06x", typedValue.data())
                INT_DEC -> typedValue.data().toString()
                INT_HEX -> "0x${typedValue.data()}"
                REFERENCE -> {
                  if (arsc != null) {
                    "@${arsc.entries[typedValue.data()]}"
                  } else {
                    typedValue.data().toString()
                  }
                }
                NULL -> "null"
                STRING -> attribute.rawValue()
                // TODO handle other formats appropriately...
                else -> typedValue.data().toString()
              }

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

      return document
    }

    private fun String.toDocument(): Document {
      return documentBuilderFactory.newDocumentBuilder()
        .parse(InputSource(StringReader(this)))
        .apply { normalizeWhitespace() }
    }

    private fun Document.normalizeWhitespace() {
      normalize()
      val emptyNodes = XPathFactory.newInstance().newXPath().evaluate(
        "//text()[normalize-space()='']",
        this,
        XPathConstants.NODESET,
      ) as NodeList
      for (emptyNode in emptyNodes) {
        emptyNode.parentNode.removeChild(emptyNode)
      }
    }

    private fun Document.toManifest(): AndroidManifest {
      val manifestElement = documentElement
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

      return AndroidManifest(toFormattedXml(), packageName, versionName, versionCode)
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
            appendLine()
            appendIndent(indent + 2)
            append(attribute.nodeName)
            append("=\"")
            append(attribute.nodeValue)
            append('"')
          }
          appendLine()
          appendIndent(indent + 2)
        }
        if (!node.hasChildNodes()) {
          append('/')
        }
        appendLine('>')

        if (node.hasChildNodes()) {
          for (child in node.childNodes) {
            appendNode(child, indent + 1)
          }

          appendIndent(indent)
          append("</")
          append(node.nodeName)
          appendLine('>')
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

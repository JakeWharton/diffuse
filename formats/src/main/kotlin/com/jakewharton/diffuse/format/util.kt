package com.jakewharton.diffuse.format

import com.google.devrel.gmscore.tools.apk.arsc.ResourceFile
import com.jakewharton.diffuse.io.Input

internal fun Input.toResourceFile() = ResourceFile(toByteArray())

/**
 * TODO: private this into `Class.kt` once we bump to Java 24+ and can use the new class file
 *   parser.
 */
internal fun parseMethod(owner: TypeDescriptor, name: String, descriptor: String): Method {
  val parameterTypes = mutableListOf<TypeDescriptor>()
  var i = 1
  while (true) {
    if (descriptor[i] == ')') {
      break
    }
    var typeIndex = i
    while (descriptor[typeIndex] == '[') {
      typeIndex++
    }
    val end =
      if (descriptor[typeIndex] == 'L') {
        descriptor.indexOf(';', startIndex = typeIndex)
      } else {
        typeIndex
      }
    val parameterDescriptor = descriptor.substring(i, end + 1)
    parameterTypes += TypeDescriptor(parameterDescriptor)
    i += parameterDescriptor.length
  }
  val returnType = TypeDescriptor(descriptor.substring(i + 1))
  return Method(owner, name, parameterTypes, returnType)
}

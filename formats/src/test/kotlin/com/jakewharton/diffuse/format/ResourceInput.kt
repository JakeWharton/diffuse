package com.jakewharton.diffuse.format

import com.jakewharton.diffuse.io.Input
import java.lang.Class
import java.net.URL
import okio.buffer
import okio.source

fun URL.asInput(): Input = ResourceInput(this)

fun Class<*>.requireResource(name: String): URL {
  return checkNotNull(getResource(name)) { "Resource $name not found." }
}

private class ResourceInput(private val url: URL) : Input {
  override val name
    get() = url.path.substringAfterLast('/')

  override fun source() = url.openStream().source().buffer()
}

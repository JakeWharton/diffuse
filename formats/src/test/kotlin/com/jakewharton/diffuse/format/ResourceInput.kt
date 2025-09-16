package com.jakewharton.diffuse.format

import com.jakewharton.diffuse.io.Input
import java.net.URL
import okio.buffer
import okio.source

fun URL.asInput(): Input = ResourceInput(this)

private class ResourceInput(
  private val url: URL,
) : Input {
  override val name get() = url.path.substringAfterLast('/')
  override fun source() = url.openStream().source().buffer()
}

package com.jakewharton.diffuse.testing

import java.net.URL

fun Class<*>.requireResource(name: String): URL {
  return checkNotNull(getResource(name)) { "Resource $name not found." }
}

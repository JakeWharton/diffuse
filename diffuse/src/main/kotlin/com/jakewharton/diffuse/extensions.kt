package com.jakewharton.diffuse

import com.android.apksig.util.DataSource
import com.android.apksig.util.DataSources
import com.google.devrel.gmscore.tools.apk.arsc.BinaryResourceFile
import com.jakewharton.diffuse.io.Input
import com.jakewharton.diffuse.io.PathInput
import java.io.Closeable
import java.nio.channels.FileChannel
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import kotlin.contracts.InvocationKind.EXACTLY_ONCE
import kotlin.contracts.contract

// TODO https://youtrack.jetbrains.com/issue/KT-18242
internal fun Path.writeText(text: String, charset: Charset = Charsets.UTF_8) = Files.write(this, text.toByteArray(charset))

internal inline fun Input.useDataSource(body: (DataSource) -> Unit) {
  contract {
    callsInPlace(body, EXACTLY_ONCE)
  }
  var closeable: Closeable? = null
  val source = when (this) {
    is PathInput -> {
      val channel = FileChannel.open(path)
      closeable = channel
      DataSources.asDataSource(channel)
    }
    else -> DataSources.asDataSource(toByteString().asByteBuffer())
  }
  try {
    body(source)
  } finally {
    closeable?.close()
  }
}

internal fun Input.toBinaryResourceFile() = BinaryResourceFile(toByteArray())

internal fun <T, R> Pair<T, T>.mapEach(body: (T) -> R): Pair<R, R> = body(first) to body(second)

// TODO replace with https://youtrack.jetbrains.com/issue/KT-20690
internal fun <T : Comparable<T>> comparingValues(): Comparator<Iterable<T>> {
  return object : Comparator<Iterable<T>> {
    override fun compare(o1: Iterable<T>, o2: Iterable<T>): Int {
      val i1 = o1.iterator()
      val i2 = o2.iterator()
      while (true) {
        if (!i1.hasNext()) {
          return if (!i2.hasNext()) 0 else -1
        }
        if (!i2.hasNext()) {
          return 1
        }
        val result = i1.next().compareTo(i2.next())
        if (result != 0) {
          return result
        }
      }
    }
  }
}

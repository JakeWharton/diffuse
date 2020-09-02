package com.jakewharton.diffuse

import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path

// TODO https://youtrack.jetbrains.com/issue/KT-18242
internal fun Path.writeText(text: String, charset: Charset = Charsets.UTF_8) = Files.write(this, text.toByteArray(charset))

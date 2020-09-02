package com.jakewharton.diffuse.format

import com.google.devrel.gmscore.tools.apk.arsc.BinaryResourceFile
import com.jakewharton.diffuse.io.Input

internal fun Input.toBinaryResourceFile() = BinaryResourceFile(toByteArray())

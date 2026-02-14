package com.jakewharton.diffuse.format

import com.google.devrel.gmscore.tools.apk.arsc.ResourceFile
import com.jakewharton.diffuse.io.Input

internal fun Input.toResourceFile() = ResourceFile(toByteArray())

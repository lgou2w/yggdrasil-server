/*
 * Copyright (C) 2019 The lgou2w (lgou2w@hotmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lgou2w.yggdrasil.util

import java.io.EOFException
import java.io.IOException
import java.io.InputStream
import java.util.*

object PNG {

    private val HEADER = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)
    private const val IHDR_LENGTH = 0x0D
    private const val IHDR = 0x49484452

    data class Metadata(val width: Int, val height: Int, val size: Int)

    @Throws(IOException::class)
    private fun readInt(input: InputStream): Int {
        val ch1 = input.read()
        val ch2 = input.read()
        val ch3 = input.read()
        val ch4 = input.read()
        if (ch1 or ch2 or ch3 or ch4 < 0)
            throw EOFException()
        return (ch1 shl 24) + (ch2 shl 16) + (ch3 shl 8) + (ch4 shl 0)
    }

    @Throws(IOException::class)
    private fun isConsistent(input: InputStream): Boolean {
        val buf = ByteArray(8)
        input.read(buf, 0, 8)
        return Arrays.equals(buf, HEADER)
    }

    @Throws(IOException::class)
    fun metadata(input: InputStream): Metadata? {
        if (!isConsistent(input)) // PNG Header
            return null
        if (readInt(input) != IHDR_LENGTH) // IHDR Length
            return null
        if (readInt(input) != IHDR) // IHDR
            return null
        val width = readInt(input)
        val height = readInt(input)
        return PNG.Metadata(width, height, input.available())
    }
}

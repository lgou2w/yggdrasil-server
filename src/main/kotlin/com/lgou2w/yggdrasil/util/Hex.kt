/*
 * Copyright (C) 2019 The lgou2w <lgou2w@hotmail.com>
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

import java.util.Random
import java.util.regex.Pattern

object Hex {

    const val HEX = 0x10
    private val RNG = Random()
    private val HEX_TABLE = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f')
    private val HEX_TABLE_BINARY = byteArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15)
    private val PATTERN = Pattern.compile("^[0-9A-Fa-f]+$")

    // 判断给定的字符串是否为十六进制字符串
    fun isMatches(hexString: String, allowBlank: Boolean = false): Boolean {
        return (hexString.isBlank() && allowBlank) ||
               PATTERN.matcher(hexString).matches()
    }

    fun generate(length: Int): String {
        val values = CharArray(length)
        (0 until length).forEach { values[it] = HEX_TABLE[RNG.nextInt(HEX)] }
        return String(values)
    }

    fun generateBinary(length: Int): ByteArray {
        val values = ByteArray(length)
        (0 until length).forEach { values[it] = HEX_TABLE_BINARY[RNG.nextInt(HEX)] }
        return values
    }

    fun encoded(values: ByteArray): String {
        val buf = StringBuffer()
        for (value in values) {
            val hex = java.lang.Integer.toHexString(value.toInt() and 0xFF)
            if (hex.length < 2) buf.append(0x0)
            buf.append(hex)
        }
        return buf.toString()
    }
}

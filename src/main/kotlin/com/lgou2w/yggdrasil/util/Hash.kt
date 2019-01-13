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

import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import javax.imageio.ImageIO

object Hash {

    const val MD5 = "MD5"
    const val SHA1 = "SHA-1"
    const val SHA256 = "SHA-256"
    const val SHA512 = "SHA-512"

    @Throws(NoSuchAlgorithmException::class)
    fun digest(algorithm: String, input: String): String {
        val md = MessageDigest.getInstance(algorithm)
        val values = input.toByteArray(Charsets.UTF_8)
        md.update(values)
        val digest = md.digest()
        return Hex.encoded(digest)
    }

    @Throws(IOException::class)
    fun computeTexture(file: File): String
            = computeTexture(ImageIO.read(file))

    @Throws(IOException::class)
    fun computeTexture(input: InputStream): String
            = computeTexture(ImageIO.read(input))

    @Throws(IOException::class)
    fun computeTexture(img: BufferedImage): String {
        val md = MessageDigest.getInstance(SHA256)
        val width = img.width
        val height = img.height
        val buff = ByteArray(4096)
        val putInt = fun (array: ByteArray, offset: Int, x: Int) {
            array[offset + 0] = (x shr 24 and 0xFF).toByte()
            array[offset + 1] = (x shr 16 and 0xFF).toByte()
            array[offset + 2] = (x shr   8 and 0xFF).toByte()
            array[offset + 3] = (x shr   0 and 0xFF).toByte()
        }
        putInt(buff, 0, width)
        putInt(buff, 4, height)
        var pos = 8
        for (x in 0 until width) {
            for (y in 0 until height) {
                putInt(buff, pos, img.getRGB(x, y))
                if (buff[pos + 0].toInt() == 0) {
                    buff[pos + 1] = 0
                    buff[pos + 2] = 0
                    buff[pos + 3] = 0
                }
                pos += 4
                if (pos == buff.size) {
                    pos = 0
                    md.update(buff, 0, buff.size)
                }
            }
        }
        if (pos > 0)
            md.update(buff, 0, buff.size)
        val digest = md.digest()
        return Hex.encoded(digest)
    }
}

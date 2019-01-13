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

package com.lgou2w.yggdrasil

import com.lgou2w.yggdrasil.util.Hash
import org.junit.Assert
import org.junit.Test

class HashTest {

    @Test
    fun testHashAndToHex() {
        val input = "123456"
        val hash = Hash.digest("MD5", input)
        println(hash)
        Assert.assertEquals(32, hash.length)
        Assert.assertEquals("e10adc3949ba59abbe56e057f20f883e", hash)
    }
}

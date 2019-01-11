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

package com.lgou2w.mcclake.yggdrasil

import com.lgou2w.mcclake.yggdrasil.cache.SimpleMemoryCached
import org.junit.Assert
import org.junit.Test

class SimpleMemoryCachedTest {

    private val cached = SimpleMemoryCached<String, Int>()

    @Test
    fun testGet() {
        cached.clear()
        cached.put("abc", 123)
        Assert.assertEquals(123, cached["abc"])
    }

    @Test
    fun testTimeout() {
        cached.clear()
        cached.put("abc", 233, 1000L) // 1 second
        Assert.assertEquals(233, cached["abc"]) // Certainly exist
        Thread.sleep(1000L) // sleep 1 second
        Assert.assertTrue(cached["abc"] == null) // true, is expired
    }
}

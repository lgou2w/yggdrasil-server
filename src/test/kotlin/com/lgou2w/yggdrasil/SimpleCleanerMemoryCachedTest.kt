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

import com.lgou2w.yggdrasil.cache.SimpleCleanerMemoryCached
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Ignore
import org.junit.Test
import java.util.concurrent.TimeUnit

class SimpleCleanerMemoryCachedTest {

    private val cached = SimpleCleanerMemoryCached<String, Int>(0L, 1L, TimeUnit.SECONDS, "t-scmct")

    @Test
    @Ignore
    fun test() {
        cached.clear()
        cached.put("a", 1, 1000L) // 1 second
        cached.put("b", 2, 2000L) // 2 seconds
        cached.put("c", 3, 3000L) // 3 seconds
        cached.put("d", 4, 4000L) // 4 seconds
        cached.put("e", 5, 5000L) // 5 seconds
        cached.setOnRemoved { println("Key '${it.first}' removed") }
        cached.start()
        runBlocking {
            while (true)
                if (cached.isEmpty())
                    break
        }
        Assert.assertTrue(cached.isEmpty())
    }

    @After
    fun close() {
        cached.close()
    }
}

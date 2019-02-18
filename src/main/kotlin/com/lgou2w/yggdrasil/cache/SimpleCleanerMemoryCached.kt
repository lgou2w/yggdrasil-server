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

package com.lgou2w.yggdrasil.cache

import com.lgou2w.ldk.common.Consumer
import java.io.Closeable
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

open class SimpleCleanerMemoryCached<K, V>(
        val initialDelay: Long,
        val period: Long,
        val timeUnit: TimeUnit,
        val threadName: String
) : SimpleMemoryCached<K, V>(), Closeable {

    private var onRemoved : Consumer<Pair<K, V>>? = null
    private var cleaner : ScheduledExecutorService? = null
    private val lock = ReentrantLock()

    var isAtWorking = false
        private set

    open fun start() {
        if (cleaner != null)
            cleaner?.shutdown()
        cleaner = Executors.newSingleThreadScheduledExecutor { r -> Thread(r, threadName) }
        cleaner?.scheduleAtFixedRate(
                this::dispatchClean,
                initialDelay,
                period,
                timeUnit
        )
        isAtWorking = true
    }

    fun setOnRemoved(callback: Consumer<Pair<K, V>>?) {
        this.onRemoved = callback
    }

    fun dispatchClean() {
        lock.lock()
        try {
            val iterator = caches.iterator()
            while (iterator.hasNext()) {
                val cached = iterator.next()
                if (cached.value.isExpired) {
                    iterator.remove()
                    onRemoved?.invoke(cached.key to cached.value.data) // callback
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            lock.unlock()
        }
    }

    override fun close() {
        isAtWorking = false
        onRemoved = null
        cleaner?.shutdown()
        cleaner = null
    }
}

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

package com.lgou2w.yggdrasil.cache

import java.util.concurrent.ConcurrentHashMap

open class SimpleMemoryCached<K, V> {

    protected val caches : ConcurrentHashMap<K, Cached<V>> = ConcurrentHashMap()

    val size : Int get() = caches.size
    val keys : Set<K> get() = caches.keys

    fun isEmpty() = caches.isEmpty()
    fun isNotEmpty() = caches.isNotEmpty()

    protected fun getCached(key: K): Cached<V>?
            = caches[key]

    open operator fun get(key: K): V? {
        val cached = getCached(key) ?: return null
        return if (cached.isExpired) null else cached.data
    }

    // 如果 timeout 小于或等于 0 那么就是常驻内存，可能不会被清除
    // 具体要看子类如何实现
    open fun put(key: K, value: V, timeout: Long = 0L): SimpleMemoryCached<K, V> {
        val expire = if (timeout <= 0L) 0L else System.currentTimeMillis() + timeout
        caches[key] = Cached(value, expire)
        return this
    }

    open fun has(key: K): Boolean
            = get(key) == null

    open fun remove(key: K) {
        caches.remove(key)
    }

    open fun clear() {
        caches.clear()
    }

    protected data class Cached<V>(
            val data: V,
            val expire: Long
    ) {
        val isExpired : Boolean
            get() {
                if (expire <= 0L) return false // 常驻缓存
                return System.currentTimeMillis() > expire
            }
    }
}

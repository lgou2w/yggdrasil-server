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

package com.lgou2w.mcclake.yggdrasil.feature

import com.lgou2w.ldk.common.Callable
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.ApplicationFeature
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.uri
import io.ktor.response.header
import io.ktor.response.respond
import io.ktor.util.AttributeKey
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class RateLimiter(config: Configuration) {

    val interval = config.interval
    val limit = config.limit
    val watchs = Collections.unmodifiableSet(config.watchs)
    val cached : MutableMap<String, Record> = ConcurrentHashMap()

    fun inWatch(uri: String): Boolean {
        return watchs.any { it.match(uri) }
    }

    fun canLimit(ip: String): Pair<Boolean, Long> {
        val record = cached[ip]
        val currentTime = System.currentTimeMillis()
        return if (record == null) {
            cached[ip] = Record().apply { reset() }
            false to 0L
        } else {
            val reset = currentTime - record.lastTimed > interval
            if (reset) {
                record.reset()
                false to 0L
            } else {
                record.increaseCount()
                val limited = !reset && record.count > limit
                limited to if (limited) record.lastTimed + interval else {
                    record.lastTimed = currentTime
                    0L
                }
            }
        }
    }

    class Configuration {
        var interval : Long = 60000L
        var limit : Int = 30
        var watchs : MutableSet<Watch<*>> = HashSet()
        inline fun watch(block: Callable<String>) { watchs.add(WatchString(block())) }
        inline fun watchRegex(block: Callable<Regex>) { watchs.add(WatchRegex(block())) }
    }

    abstract class Watch<T>(val target: T) : Comparable<T> {
        abstract fun match(source: String): Boolean
    }

    class WatchString(target: String) : Watch<String>(target) {
        override fun match(source: String): Boolean = source == target
        override fun compareTo(other: String): Int = target.compareTo(other)
    }

    class WatchRegex(target: Regex) : Watch<Regex>(target) {
        override fun match(source: String): Boolean = target.matches(source)
        override fun compareTo(other: Regex): Int = target.pattern.compareTo(other.pattern)
    }

    class Record {
        var count = 0
        var lastTimed = 0L
        fun increaseCount() {
            count++
        }
        fun reset() {
            count = 1
            lastTimed = System.currentTimeMillis()
        }
    }

    companion object Feature : ApplicationFeature<ApplicationCallPipeline, Configuration, RateLimiter> {
        override val key: AttributeKey<RateLimiter> = AttributeKey("RateLimiter")
        override fun install(pipeline: ApplicationCallPipeline, configure: Configuration.() -> Unit): RateLimiter {
            val feature = RateLimiter(Configuration().also(configure))
            pipeline.intercept(ApplicationCallPipeline.Monitoring) {
                if (feature.inWatch(call.request.uri)) {
                    val ip = call.request.local.remoteHost
                    val (limited, retryAfter) = feature.canLimit(ip)
                    if (limited) {
                        call.response.header("Retry-After", retryAfter)
                        call.respond(HttpStatusCode.TooManyRequests)
                        finish()
                    }
                }
            }
            return feature
        }
    }
}

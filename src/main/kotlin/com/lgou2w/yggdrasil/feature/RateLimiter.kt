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

package com.lgou2w.yggdrasil.feature

import com.lgou2w.ldk.common.Applicator
import com.lgou2w.ldk.common.Callable
import com.lgou2w.ldk.common.SuspendBiFunction
import com.lgou2w.ldk.common.SuspendFunction
import io.ktor.application.ApplicationCall
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.ApplicationFeature
import io.ktor.http.HttpStatusCode
import io.ktor.request.uri
import io.ktor.response.header
import io.ktor.response.respond
import io.ktor.util.AttributeKey
import io.ktor.util.pipeline.PipelineContext
import io.ktor.util.pipeline.PipelinePhase
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class RateLimiter(configuration: Configuration) {

    private val limiters = configuration.limiters.map { src ->
        val dest = Limiter()
        dest.interval = src.interval
        dest.threshold = src.threshold
        dest.cached = ConcurrentHashMap()
        dest.pipeline = src.pipeline
        dest.key = src.key
        dest.watchs = Collections.unmodifiableSet(src.watchs)
        dest.watchsRegex = Collections.unmodifiableSet(src.watchsRegex)
        dest.limited = src.limited
        dest
    }

    class Configuration {

        internal var limiters : MutableList<Limiter> = ArrayList()

        fun limit(limiter: Applicator<Limiter>) {
            limiters.add(Limiter().also(limiter))
        }
    }

    // TODO 缓存控制

    class Limiter {

        var interval : Long = 60000L
        var threshold : Int = 30
        var pipeline : PipelinePhase = ApplicationCallPipeline.Monitoring
        var limitedStatus : HttpStatusCode = HttpStatusCode.TooManyRequests

        internal var cached : MutableMap<String, Record> = HashMap(0)
        internal var key : SuspendFunction<ApplicationCall, String> = { call -> call.request.local.remoteHost }
        internal var watchs : MutableSet<String> = HashSet()
        internal var watchsRegex : MutableSet<Regex> = HashSet()
        internal var limited : SuspendBiFunction<ApplicationCall, String, Unit>? = null

        fun key(block: SuspendFunction<ApplicationCall, String>) { key = block }
        fun watch(block: Callable<String>) { watchs.add(block()) }
        fun watchRegex(block: Callable<Regex>) { watchsRegex.add(block()) }
        fun limitedBefore(block: SuspendBiFunction<ApplicationCall, String, Unit>?) { limited = block }
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

    private fun inWatch(limiter: Limiter, uri: String): Boolean {
        return limiter.watchs.any { it == uri } ||
               limiter.watchsRegex.any { it.matches(uri) }
    }

    private fun canLimit(limiter: Limiter, key: String): Pair<Boolean, Long> {
        val record = limiter.cached[key]
        val currentTime = System.currentTimeMillis()
        return if (record == null) {
            limiter.cached[key] = Record().apply { reset() }
            false to 0L
        } else {
            val reset = currentTime - record.lastTimed > limiter.interval
            if (reset) {
                record.reset()
                false to 0L
            } else {
                record.increaseCount()
                val limited = !reset && record.count > limiter.threshold
                limited to if (limited) record.lastTimed + limiter.interval else {
                    record.lastTimed = currentTime
                    0L
                }
            }
        }
    }

    internal suspend fun intercept(limiter: Limiter, context: PipelineContext<Unit, ApplicationCall>) {
        if (inWatch(limiter, context.context.request.uri)) {
            val key = limiter.key(context.context)
            val (limited, retryAfter) = canLimit(limiter, key)
            if (limited) {
                limiter.limited?.invoke(context.context, key)
                context.context.response.header("Retry-After", retryAfter)
                context.context.respond(limiter.limitedStatus)
                context.finish()
            }
        }
    }

    companion object Feature : ApplicationFeature<ApplicationCallPipeline, Configuration, RateLimiter> {

        override val key: AttributeKey<RateLimiter> = AttributeKey("Rate Limiter")
        override fun install(pipeline: ApplicationCallPipeline, configure: Configuration.() -> Unit): RateLimiter {
            val configuration = Configuration().also(configure)
            val feature = RateLimiter(configuration)
            feature.limiters.forEach { limiter ->
                pipeline.intercept(limiter.pipeline) {
                    feature.intercept(limiter, this)
                }
            }
            return feature
        }
    }
}

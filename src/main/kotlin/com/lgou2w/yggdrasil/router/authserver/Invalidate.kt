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

package com.lgou2w.yggdrasil.router.authserver

import com.lgou2w.yggdrasil.controller.AuthController
import com.lgou2w.yggdrasil.router.RouterHandler
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.accept
import io.ktor.routing.post

/**
 * ## 撤消令牌时的 POST 请求
 *
 * 请求:
 * ```json
 * {
 *   "accessToken": "valid accessToken",
 *   "clientToken": "client identifier", (optional)
 * }
 * ```
 *
 * 响应: 始终 HTTP 204
 *
 * @see [https://wiki.vg/Authentication#Invalidate]
 */
object Invalidate : RouterHandler {

    override val method = "POST"
    override val path = "/authserver/invalidate"

    override fun install(routing: Routing) {
        routing.accept(ContentType.Application.Json) {
            routing.post(path) {
                val request : Request? = call.receive()
                try {
                    AuthController.invalidate(request?.accessToken, request?.clientToken)
                } catch (e: Exception) {
                } finally {
                    call.respond(HttpStatusCode.NoContent) // 总是 204, 不管操作是否成功
                }
            }
        }
    }

    private data class Request(
            val accessToken: String?,
            val clientToken: String?
    )
}

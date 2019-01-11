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

package com.lgou2w.mcclake.yggdrasil.router.authserver

import com.lgou2w.mcclake.yggdrasil.controller.AuthController
import com.lgou2w.mcclake.yggdrasil.router.RouterHandler
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.accept
import io.ktor.routing.post

/**
 * ## 验证新注册时的 POST 请求
 *
 * * 速率限制器: 键 = ip
 * * 请求:
 * ```json
 * {
 *   "username": "user email",
 *   "nickname": "user nickname"
 * }
 * ```
 *
 * @see [Register]
 */
object RegisterVerify : RouterHandler {

    override val method: String = "POST"
    override val path: String = "/authserver/registerVerify"

    override fun install(routing: Routing) {
        routing.accept(ContentType.Application.Json) {
            routing.post(path) {
                val parameters = call.receive<Map<String, Any?>>()
                val email = parameters["username"]?.toString()
                val nickname = parameters["nickname"]?.toString()
                val response = AuthController.registerVerify(email, nickname)
                call.respond(response)
            }
        }
    }
}
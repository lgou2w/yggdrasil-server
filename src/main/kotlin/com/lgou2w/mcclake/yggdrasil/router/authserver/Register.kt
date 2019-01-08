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
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.accept
import io.ktor.routing.post

/**
 * ## POST request when register a new user.
 *
 * * RateLimiter: key = ip
 * * Request:
 * ```json
 * {
 *   "username": "user email",
 *   "password": "user password",
 *   "nickname": "user nickname" (optional)
 * }
 * ```
 */
object Register : RouterHandler {

    override val method: String = "POST"
    override val path: String = "/authserver/register"

    override fun install(routing: Routing) {
        routing.accept(ContentType.Application.Json) {
            routing.post(path) {
                if (!yggdrasilService.conf.userRegistrationEnable) {
                    // Registration is not allowed
                    // Response 405 Method Not Allowed
                    call.respond(HttpStatusCode.MethodNotAllowed)
                } else {
                    val parameters = call.receive<Map<String, Any?>>()
                    val email = parameters["username"]?.toString()
                    val password = parameters["password"]?.toString()
                    val nickname = parameters["nickname"]?.toString()
                    val response = AuthController.register(email, password, nickname)
                    call.respond(response)
                }
            }
        }
    }
}

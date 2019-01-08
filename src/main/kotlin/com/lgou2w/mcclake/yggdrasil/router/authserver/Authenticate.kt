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
 * ## POST request when the client uses a username and password for authentication.
 *
 * * RateLimiter: key = username
 * * Request:
 * ```json
 * {
 *   "username": "user email",
 *   "password": "user password",
 *   "clientToken": "client identifier", (optional)
 *   "requestUser": true/false, (optional)
 *   "agent": {
 *     "name": "minecraft",
 *     "version": 1
 *   }
 * }
 * ```
 *
 * @see [https://wiki.vg/Authentication#Authenticate]
 */
object Authenticate : RouterHandler {

    override val method = "POST"
    override val path = "/authserver/authenticate"

    override fun install(routing: Routing) {
        routing.accept(ContentType.Application.Json) {
            routing.post(path) {
                val request : Request? = call.receive()
                val response = AuthController.authenticate(
                        request?.username,
                        request?.password,
                        request?.clientToken,
                        request?.requestUser
                )
                call.respond(response)
            }
        }
    }

    private data class Request(
            val username: String?,
            val password: String?,
            val clientToken: String?,
            val requestUser: Boolean?,
            val agent: Agent?
    ) {
        data class Agent(
                val name: String?,
                val version: Int?
        )
    }
}

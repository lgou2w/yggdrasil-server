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
 * ## 用户刷新令牌时的 POST 请求
 *
 * 请求:
 * ```json
 * {
 *   "accessToken": "valid accessToken",
 *   "clientToken": "client identifier", (optional)
 *   "requestUser": true/false, (optional)
 *   "selectedProfile": { (optional)
 *     "id": "profile identifier",
 *     "name": "player name"
 *   }
 * }
 * ```
 *
 * @see [https://wiki.vg/Authentication#Refresh]
 */
object Refresh : RouterHandler {

    override val method = "POST"
    override val path = "/authserver/refresh"

    override fun install(routing: Routing) {
        routing.accept(ContentType.Application.Json) {
            routing.post(path) {
                val request : Request? = call.receive()
                val response = AuthController.refresh(
                        request?.accessToken,
                        request?.clientToken,
                        request?.requestUser,
                        request?.selectedProfile?.id,
                        request?.selectedProfile?.name
                )
                call.respond(response)
            }
        }
    }

    private data class Request(
            val accessToken: String?,
            val clientToken: String?,
            val requestUser: Boolean?,
            val selectedProfile: Profile?
    ) {
        data class Profile(
                val id: String?,
                val name: String?
        )
    }
}

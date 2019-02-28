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

package com.lgou2w.yggdrasil.router.sessionserver

import com.lgou2w.yggdrasil.controller.SessionController
import com.lgou2w.yggdrasil.router.RouterHandler
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get

/**
 * ## 服务器验证客户端是否已加入服务器时的 GET 请求
 *
 * 请求:
 * ```properties
 * username = "profile identifier"
 * serverId = "server id"
 * ip = "client ip" (可选) // TODO
 * ```
 *
 * 响应: HTTP 204 如果操作失败
 *
 * @see [https://wiki.vg/Protocol_Encryption#Server]
 */
object HasJoined : RouterHandler {

    override val method = "GET"
    override val path = "/sessionserver/session/minecraft/hasJoined"

    override fun install(routing: Routing) {
        routing.get(path) {
            val username = call.request.queryParameters["username"]
            val serverId = call.request.queryParameters["serverId"]
            val userIp = call.request.queryParameters["ip"]
            try {
                val response = SessionController.hasJoinedServer(
                        call.request.local,
                        username,
                        serverId,
                        userIp
                )
                call.respond(response)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.NoContent) // 总是 204, 当操作失败
            }
        }
    }
}

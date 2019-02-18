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

import com.lgou2w.yggdrasil.router.RouterHandler
import io.ktor.routing.Routing
import io.ktor.routing.post

/**
 * ## 客户端连接到服务器时的 POST 请求
 *
 * 请求:
 * ```json
 * {
 *   "accessToken": "access token",
 *   "selectedProfile": "player uuid without dashes",
 *   "serverId": "server id"
 * }
 * ```
 *
 * 响应: HTTP 204 如果操作成功
 *
 * @see [https://wiki.vg/Protocol_Encryption#Client]
 */
object Join : RouterHandler {

    override val method = "POST"
    override val path = "/sessionserver/session/minecraft/join"

    override fun install(routing: Routing) {
        routing.post(path) {
            TODO()
        }
    }
}

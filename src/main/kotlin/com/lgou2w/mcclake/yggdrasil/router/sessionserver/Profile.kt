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

package com.lgou2w.mcclake.yggdrasil.router.sessionserver

import com.lgou2w.mcclake.yggdrasil.router.RouterHandler
import io.ktor.routing.Routing
import io.ktor.routing.get

/**
 * ## 客户端查询用户档案时的 GET 请求
 *
 * 请求:
 * ```properties
 * unsigned = true/false (可选) // TODO
 * ```
 *
 * 响应: HTTP 204 如果用户没有存在
 *
 * @see [https://wiki.vg/Protocol_Encryption#Client]
 */
object Profile : RouterHandler {

    override val method = "GET"
    override val path = "/sessionserver/session/minecraft/profile/{uuid}"

    override fun install(routing: Routing) {
        routing.get(path) {
            TODO()
        }
    }
}

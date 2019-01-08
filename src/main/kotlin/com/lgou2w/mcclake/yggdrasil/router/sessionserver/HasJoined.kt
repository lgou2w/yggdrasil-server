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
 * ## GET request when the server authenticates the client.
 *
 * Request:
 * ```properties
 * username = "profile identifier"
 * serverId = "server id"
 * ip = "client ip" (optional)
 * ```
 *
 * Response: 204 if failed
 *
 * @see [https://wiki.vg/Protocol_Encryption#Server]
 */
object HasJoined : RouterHandler {

    override val method = "GET"
    override val path = "/sessionserver/session/minecraft/hasJoined"

    override fun install(routing: Routing) {
        routing.get(path) {
            TODO()
        }
    }
}

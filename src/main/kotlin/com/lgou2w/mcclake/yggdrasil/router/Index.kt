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

package com.lgou2w.mcclake.yggdrasil.router

import com.lgou2w.mcclake.yggdrasil.Yggdrasil
import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get

object Index : RouterHandler {

    override val path: String = "/"
    override val method: String = "GET"

    override fun install(routing: Routing) {
        routing.get("/") {
            call.respond(mapOf(
                    "meta" to mapOf(
                            "serverName" to "MCCLake Yggdrasil Server",
                            "implementationName" to "mcclake-yggdrasil-server",
                            "implementationVersion" to Yggdrasil.VERSION.version
                    ),
                    "skinDomains" to emptyArray<String>(),
                    "signaturePublickey" to "NONE",
                    "routers" to Routers.yggdrasilPaths
            ))
        }
    }
}

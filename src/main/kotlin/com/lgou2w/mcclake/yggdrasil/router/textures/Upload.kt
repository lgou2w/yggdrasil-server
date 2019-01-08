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

package com.lgou2w.mcclake.yggdrasil.router.textures

import com.lgou2w.mcclake.yggdrasil.YggdrasilLog
import com.lgou2w.mcclake.yggdrasil.router.RouterHandler
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.content.PartData
import io.ktor.http.content.readAllParts
import io.ktor.request.receiveMultipart
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.accept
import io.ktor.routing.post

/**
 * ## POST request when upload skin texture.
 */
object Upload : RouterHandler {

    override val method: String = "POST"
    override val path: String = "/textures/upload"

    override fun install(routing: Routing) {
        routing.accept(ContentType.MultiPart.FormData) {
            post(path) {
                val multipart = call.receiveMultipart()
                val parts = multipart.readAllParts()
                val model = parts[0] as PartData.FormItem
                val file = parts[1] as PartData.FileItem
                YggdrasilLog.info("Model => ${model.value}")
                YggdrasilLog.info("File => ${file.originalFileName}")
                model.dispose()
                file.dispose()
                call.respond(mapOf("ok" to true))
            }
        }
    }
}

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

package com.lgou2w.yggdrasil.router.textures

import com.lgou2w.yggdrasil.router.RouterHandler
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.LocalFileContent
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get

/**
 * ## 获取皮肤纹理时的 GET 请求
 *
 * 请求:
 * ```properties
 * hash = 64-character texture hash value.
 * ```
 */
object Obtain : RouterHandler {

    override val method: String = "GET"
    override val path: String = "/textures/{hash}"

    override fun install(routing: Routing) {
        routing.get(path) {
            val hash = call.parameters["hash"]
            if (hash == null || hash.length != 64) {
                call.respond(HttpStatusCode.NotFound)
            } else {
                val textureFile = yggdrasilService.manager.texturesManager.file(hash)
                if (textureFile.isFile && textureFile.exists())
                    call.respond(LocalFileContent(textureFile, ContentType.Image.PNG))
                else
                    call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}

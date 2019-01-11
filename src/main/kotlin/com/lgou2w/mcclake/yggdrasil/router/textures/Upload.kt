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

import com.lgou2w.mcclake.yggdrasil.router.RouterHandler
import io.ktor.http.ContentType
import io.ktor.routing.Routing
import io.ktor.routing.accept
import io.ktor.routing.post

/**
 * ## 上传皮肤纹理时的 POST 请求
 */
object Upload : RouterHandler {

    override val method: String = "POST"
    override val path: String = "/textures/upload"

    override fun install(routing: Routing) {
        routing.accept(ContentType.MultiPart.FormData) {
            post(path) {
                TODO()
            }
        }
    }
}

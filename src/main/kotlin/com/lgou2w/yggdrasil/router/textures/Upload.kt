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

package com.lgou2w.yggdrasil.router.textures

import com.lgou2w.ldk.common.isTrue
import com.lgou2w.yggdrasil.controller.AuthController
import com.lgou2w.yggdrasil.controller.ProfileController
import com.lgou2w.yggdrasil.error.UnauthorizedException
import com.lgou2w.yggdrasil.router.RouterHandler
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.request.header
import io.ktor.request.receiveMultipart
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.accept
import io.ktor.routing.put

/**
 * ## 上传皮肤纹理时的 PUT 请求
 *
 * 请求:
 * ```header
 * Authorization: Bearer <AccessToken>
 * ```
 * ```multiPart
 * FormItem = UUID
 * FormItem = ModelType
 * FormItem = TextureType
 * FileItem = TextureFile
 * ```
 *
 * 响应: 200 如果成功
 */
object Upload : RouterHandler {

    override val method: String = "PUT"
    override val path: String = "/textures/upload"

    override fun install(routing: Routing) {
        routing.accept(ContentType.MultiPart.FormData) {
            put(path) {
                val bearerToken = call.request.header(HttpHeaders.Authorization)?.split(" ")
                val accessToken = bearerToken?.getOrNull(1)
                if (bearerToken?.getOrNull(0) != "Bearer" || accessToken?.isBlank().isTrue())
                    throw UnauthorizedException()

                // 验证访问令牌是否有效，否则抛出异常
                // 其实并不需要做 if 判断，因为内部如果失效直接抛出异常了
                val token = AuthController.validate(accessToken, null)

                val multiPart = call.receiveMultipart()
                val uuid = multiPart.readPart() as? PartData.FormItem
                val model = multiPart.readPart() as? PartData.FormItem
                val texture = multiPart.readPart() as? PartData.FormItem
                val textureFile = multiPart.readPart() as? PartData.FileItem

                try {
                    ProfileController.uploadTexture(
                            token,
                            uuid,
                            model,
                            texture,
                            textureFile
                    )
                    call.respond(HttpStatusCode.OK) // 操作成功，返回 HTTP 200 OK
                } catch (e: Exception) {
                    // 直接抛出异常，不用管
                    throw e
                } finally {
                    // 最后释放这些多部分数据
                    uuid?.dispose?.invoke()
                    model?.dispose?.invoke()
                    texture?.dispose?.invoke()
                    textureFile?.dispose?.invoke()
                }
            }
        }
    }
}

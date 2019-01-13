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

import com.lgou2w.yggdrasil.YggdrasilLog
import com.lgou2w.yggdrasil.error.ForbiddenOperationException
import com.lgou2w.yggdrasil.router.RouterHandler
import com.lgou2w.yggdrasil.util.Hash
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.readAllParts
import io.ktor.http.content.streamProvider
import io.ktor.request.receiveMultipart
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.accept
import io.ktor.routing.put
import java.io.BufferedInputStream

/**
 * ## 上传皮肤纹理时的 PUT 请求
 */
object Upload : RouterHandler {

    override val method: String = "PUT"
    override val path: String = "/textures/upload"

    override fun install(routing: Routing) {
        routing.accept(ContentType.MultiPart.FormData) {
            put(path) {

                // TODO 令牌验证、图片验证、支持披风和鞘翅、等等

                try {
                    val multipart = call.receiveMultipart()
                    val parts = multipart.readAllParts()
                    val partModel = parts.getOrNull(0) as? PartData.FormItem
                    val partFile = parts.getOrNull(1) as? PartData.FileItem

                    if (partModel == null || partFile == null)
                        throw ForbiddenOperationException("无效的表单数据.")

                    try {

                        val input = partFile.streamProvider()
                        val bufferedInput = BufferedInputStream(input)
                        bufferedInput.mark(0)

                        val hash = Hash.computeTexture(bufferedInput)
                        val outFile = yggdrasilService.manager.texturesManager.file(hash)

                        YggdrasilLog.info("计算材质的哈希值为 : $hash")

                        bufferedInput.reset()
                        bufferedInput.use { its ->
                            outFile.outputStream().buffered().use { ots ->
                                its.copyTo(ots)
                            }
                        }

                        bufferedInput.close()
                        call.respond(HttpStatusCode.OK)

                    } catch (e: Exception) {
                        throw e
                    } finally {
                        // 释放
                        partModel.dispose()
                        partFile.dispose()
                    }

                } catch (e: Exception) {
                    YggdrasilLog.error("处理上传文件时错误:", e)
                    call.respond(HttpStatusCode.InternalServerError)
                }
            }
        }
    }
}

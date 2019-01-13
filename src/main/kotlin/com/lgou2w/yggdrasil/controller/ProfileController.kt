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

package com.lgou2w.yggdrasil.controller

import com.lgou2w.ldk.common.orTrue
import com.lgou2w.yggdrasil.YggdrasilLog
import com.lgou2w.yggdrasil.dao.ModelType
import com.lgou2w.yggdrasil.dao.Player
import com.lgou2w.yggdrasil.dao.Texture
import com.lgou2w.yggdrasil.dao.TextureType
import com.lgou2w.yggdrasil.dao.Textures
import com.lgou2w.yggdrasil.error.ForbiddenOperationException
import io.ktor.http.RequestConnectionPoint
import java.util.*
import kotlin.collections.LinkedHashMap

object ProfileController : Controller() {

    const val INVALID_PROFILE_NOT_EXISTED = "无效档案. 未存在."

    const val INVALID_K_PROFILE_ID = "档案 Id"

    const val M_LOOKUP = "尝试获取角色档案 : uuid = {}, unsigned = {}"

    private fun texturesBase64(
            point: RequestConnectionPoint,
            profile: Player,
            textures: List<Texture>,
            unsigned: Boolean
    ): String {
        val texturesResponse = LinkedHashMap<String, Any?>()
        texturesResponse["timestamp"] = System.currentTimeMillis() / 1000L
        texturesResponse["profileId"] = profile.id.value
        texturesResponse["profileName"] = profile.name
        if (!unsigned)
            texturesResponse["signatureRequired"] = true
        val texturesData =  LinkedHashMap<String, Any?>(textures.size)
        textures.forEach { texture ->
            texturesData[texture.type.name] = mapOf(
                    "url" to manager.texturesManager.wrapUrl(texture.url,
                            point.scheme, point.host, point.port // 包装材质 URL
                    ),
                    "metadata" to if (texture.type == TextureType.SKIN && profile.model == ModelType.ALEX)
                        mapOf("model" to "slim") else null
            )
        }
        texturesResponse["textures"] = texturesData
        val json = manager.gson.toJson(texturesResponse)
        val values = json.toByteArray(Charsets.UTF_8)
        return Base64.getEncoder().encodeToString(values)
    }

    private fun texturesSignature(texturesBase64: String): String {
        // TODO 材质签名
        return ""
    }

    private fun texturesProperty(
            point: RequestConnectionPoint,
            profile: Player,
            textures: List<Texture>,
            unsigned: Boolean
    ): List<Map<String, Any?>> {
        if (textures.isEmpty()) return emptyList()
        val texturesBase64 = texturesBase64(point, profile, textures, unsigned)
        val property = LinkedHashMap<String, Any?>()
        property["name"] = "textures"
        property["value"] = texturesBase64
        if (!unsigned)
            property["signature"] = texturesSignature(texturesBase64)
        return Collections.singletonList(property)
    }

    @Throws(ForbiddenOperationException::class)
    suspend fun lookupProfile(
            point: RequestConnectionPoint,
            uuid: String?,
            unsigned: Boolean?
    ): Map<String, Any?> {

        YggdrasilLog.info(M_LOOKUP, uuid, unsigned)

        val uuid0 = checkIsNonUnsignedUUID(uuid, INVALID_K_PROFILE_ID)
        val (profile, textures) = transaction {
            val profile = Player.findById(uuid0) ?: throw ForbiddenOperationException(INVALID_PROFILE_NOT_EXISTED)
            val textures = Texture.find { Textures.player eq profile.id }.toList()
            profile to textures
        }

        // TODO 缓存功能

        return mapOf(
                "id" to profile.id.value,
                "name" to profile.name,
                "properties" to texturesProperty(point,
                        profile,
                        textures,
                        unsigned.orTrue()
                )
        )
    }
}

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

package com.lgou2w.yggdrasil.controller

import com.lgou2w.ldk.common.Enums
import com.lgou2w.ldk.common.letIfNotNull
import com.lgou2w.ldk.common.orTrue
import com.lgou2w.yggdrasil.YggdrasilLog
import com.lgou2w.yggdrasil.dao.ModelType
import com.lgou2w.yggdrasil.dao.Player
import com.lgou2w.yggdrasil.dao.Texture
import com.lgou2w.yggdrasil.dao.TextureType
import com.lgou2w.yggdrasil.dao.Textures
import com.lgou2w.yggdrasil.dao.Token
import com.lgou2w.yggdrasil.error.ForbiddenOperationException
import com.lgou2w.yggdrasil.util.Hash
import com.lgou2w.yggdrasil.util.PNG
import com.lgou2w.yggdrasil.util.UUIDSerializer
import io.ktor.http.RequestConnectionPoint
import io.ktor.http.content.PartData
import io.ktor.http.content.streamProvider
import org.jetbrains.exposed.sql.and
import java.io.BufferedInputStream
import java.io.IOException
import java.util.Base64
import java.util.Collections
import java.util.LinkedHashMap

object ProfileController : Controller() {

    const val INVALID_PROFILE_NOT_EXISTED = "无效档案. 未存在."
    const val INVALID_MULTIPART_DATA = "无效的表单数据."
    const val INVALID_ILLEGAL_TEXTURE = "无效的非法材质图片."
    const val INVALID_ILLEGAL_TEXTURE_SIZE = "无效的非法字节大小."

    const val INVALID_K_PROFILE_ID = "档案 Id"
    const val INVALID_K_PART_MODEL = "模型类型"
    const val INVALID_K_PART_TEXTURE = "材质类型"
    const val INVALID_K_PART_TEXTURE_FILE = "材质文件"

    const val M_LOOKUP = "尝试获取角色档案 : uuid = {}, unsigned = {}"
    const val M_UPLOAD_0 = "尝试上传角色材质 : uuid = {}, 模型 = {}, 类型 = {}"
    const val M_UPLOAD_1 = "上传角色材质成功, 材质哈希值 : {}"

    private fun texturesValue(
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
                    "url" to manager.texturesManager.wrapUrl(texture,
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

    private fun texturesProperty(
            point: RequestConnectionPoint,
            profile: Player,
            textures: List<Texture>,
            unsigned: Boolean
    ): List<Map<String, Any?>> {
        if (textures.isEmpty()) return emptyList()
        val texturesValue= texturesValue(point, profile, textures, unsigned)
        val property = LinkedHashMap<String, Any?>()
        property["name"] = "textures"
        property["value"] = texturesValue
        if (!unsigned)
            property["signature"] = manager.texturesManager.signature(texturesValue)
        return Collections.singletonList(property)
    }

    @Throws(ForbiddenOperationException::class, IOException::class)
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

    // 注意：请调用者自己释放 PartData
    @Throws(ForbiddenOperationException::class, IOException::class)
    suspend fun uploadTexture(
            token: Token,
            uuid: PartData.FormItem?,
            model: PartData.FormItem?,
            texture: PartData.FormItem?,
            textureFile: PartData.FileItem?
    ) {
        val uuid0 = checkIsNonUnsignedUUID(uuid?.value, INVALID_K_PROFILE_ID)
        val model0 = model?.value.letIfNotNull { Enums.ofName(ModelType::class.java, this) }
                     ?: throw ForbiddenOperationException(INVALID_MULTIPART_DATA + INVALID_K_PART_MODEL)
        val texture0 = texture?.value.letIfNotNull { Enums.ofName(TextureType::class.java, this) }
                       ?: throw ForbiddenOperationException(INVALID_MULTIPART_DATA + INVALID_K_PART_TEXTURE)
        val textureInput = textureFile?.streamProvider
                           ?: throw ForbiddenOperationException(INVALID_MULTIPART_DATA + INVALID_K_PART_TEXTURE_FILE)

        YggdrasilLog.info(M_UPLOAD_0, UUIDSerializer.fromUUID(uuid0), model0, texture0)

        val input = textureInput()
        val buf = BufferedInputStream(input).apply { mark(0) } // 标记，用于重置
        val metadata = PNG.metadata(buf)

        if (metadata == null || metadata.width != 64 || metadata.height != 64)
            throw ForbiddenOperationException(INVALID_ILLEGAL_TEXTURE)
        if (metadata.size > conf.userTexturesMaxFileSize)
            throw ForbiddenOperationException(INVALID_ILLEGAL_TEXTURE_SIZE)

        // 图片验证通过，重置标记然后重新标记并计算哈希值
        buf.reset()
        buf.mark(0)
        val hash = Hash.computeTexture(buf)

        transaction {
            val profile = Player.findById(uuid0) ?: throw ForbiddenOperationException(INVALID_PROFILE_NOT_EXISTED)
            val destTexture = Texture.find { Textures.player eq profile.id and (Textures.type eq texture0) }.limit(1).firstOrNull()
            if (destTexture == null)
                Texture.new {
                    this.type = texture0
                    this.url = hash
                    this.player = profile
                }
            else {
                destTexture.type = texture0
                destTexture.url = hash
            }
            profile.model = model0
        }

        // 重置标记然后保存材质到本地
        val outFile = manager.texturesManager.file(hash)
        buf.reset()
        buf.use { its ->
            outFile.outputStream().use { ots ->
                its.copyTo(ots)
            }
        }

        // 上传成功
        YggdrasilLog.info(M_UPLOAD_1, hash)
    }
}

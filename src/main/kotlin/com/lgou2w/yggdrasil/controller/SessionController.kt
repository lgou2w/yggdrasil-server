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

import com.lgou2w.yggdrasil.YggdrasilLog
import com.lgou2w.yggdrasil.cache.SimpleCleanerMemoryCached
import com.lgou2w.yggdrasil.dao.Player
import com.lgou2w.yggdrasil.dao.Texture
import com.lgou2w.yggdrasil.dao.Textures
import com.lgou2w.yggdrasil.dao.Token
import com.lgou2w.yggdrasil.error.ForbiddenOperationException
import io.ktor.http.RequestConnectionPoint
import java.util.UUID
import java.util.concurrent.TimeUnit

object SessionController : Controller() {

    const val INVALID_SERVER_ID = "无效的服务器会话 Id."
    const val INVALID_SESSION = "无效的加入会话."
    const val INVALID_IP = "无效的用户 Ip, 不匹配."
    const val INVALID_USERNAME = "无效的用户名, 未存在."

    const val M_JOIN_1 = "尝试加入服务器 : accessToken = {}, selectedProfile = {}, serverId = {}, userIp = {}"
    const val M_JOIN_2 = "用户加入服务器会话已建立 : serverId = {}"
    const val M_HAS_JOINED_1 = "尝试判断加入服务器 : username = {}, serverId = {}, userIp = {}"

    private val sessionTimeoutDefault = 30L // 30 秒
    private val sessionCached by lazy {
        object : SimpleCleanerMemoryCached<String, Session>(
                sessionTimeoutDefault,
                // 每次间隔的时间，默认超时时间的 1.5 倍
                // 假设 5 分钟超时，那么每隔 7.5 分钟清理一次
                Math.round(sessionTimeoutDefault * 1.5),
                TimeUnit.SECONDS, "session"
        ) {
            val sessionTimeout = sessionTimeoutDefault * 1000L // 毫秒

            init {
                setOnRemoved { session ->
                    YggdrasilLog.info("用户加入服务器会话已过期: ${session.second.selectedProfile} = ${session.first}")
                }
            }

            fun saveSession(
                    serverId: String,
                    accessToken: UUID,
                    selectedProfile: UUID,
                    userIp: String
            ): Session {
                val session = Session(serverId, accessToken, selectedProfile, userIp)
                put(serverId, session, sessionTimeout)
                return session
            }
        }
    }

    private data class Session(
            val serverId: String,
            val accessToken: UUID,
            val selectedProfile: UUID,
            val userIp: String
    )

    @Throws(ForbiddenOperationException::class)
    suspend fun joinServer(
            accessToken: String?,
            selectedProfile: String?,
            serverId: String?,
            userIp: String
    ) {

        YggdrasilLog.info(M_JOIN_1, accessToken, selectedProfile, serverId, userIp)

        val serverId0 = serverId ?: throw ForbiddenOperationException(INVALID_SERVER_ID)
        val accessToken0 = checkIsNonUnsignedUUID(accessToken, AuthController.INVALID_K_ACCESS_TOKEN)
        val selectedProfile0 = checkIsNonUnsignedUUID(selectedProfile, AuthController.INVALID_K_PROFILE_ID)

        val token = transaction {
            Player.findById(selectedProfile0)
                ?: throw ForbiddenOperationException(ProfileController.INVALID_PROFILE_NOT_EXISTED)
            val token = Token.findById(accessToken0)
                ?: throw ForbiddenOperationException(AuthController.INVALID_TOKEN_NOT_EXISTED)
            token.user.checkIsNotBanned() // 检查是否已被封禁
            token
        }
        if (!token.isValid(conf.userTokenValidMillis))
            throw ForbiddenOperationException(AuthController.INVALID_TOKEN_EXPIRED)

        sessionCached.saveSession(
                serverId0,
                accessToken0,
                selectedProfile0,
                userIp
        )
        YggdrasilLog.info(M_JOIN_2, serverId0)
    }

    @Throws(ForbiddenOperationException::class)
    suspend fun hasJoinedServer(
            point: RequestConnectionPoint,
            username: String?,
            serverId: String?,
            userIp: String?
    ): Map<String, Any?> {

        YggdrasilLog.info(M_HAS_JOINED_1, username, serverId, userIp)

        val username0 = checkIsValidNickname(username)
        val serverId0 = serverId ?: throw ForbiddenOperationException(INVALID_SERVER_ID)
        val session = sessionCached[serverId0] ?: throw ForbiddenOperationException(INVALID_SESSION)
        if (userIp != null && userIp != session.userIp)
            throw ForbiddenOperationException(INVALID_IP)
        val (profile, textures) = transaction {
            val profile = Player.findById(session.selectedProfile)
                ?: throw ForbiddenOperationException(ProfileController.INVALID_PROFILE_NOT_EXISTED)
            if (profile.name != username0)
                throw ForbiddenOperationException(INVALID_USERNAME)
            val textures = Texture.find { Textures.player eq profile.id }.toList()
            profile to textures
        }
        val properties = ProfileController.texturesProperty(point, profile, textures, unsigned = false) // 需要进行签名
        return mapOf(
                "id" to profile.id.value,
                "name" to profile.name,
                "properties" to properties
        )
    }
}

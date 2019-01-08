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

package com.lgou2w.mcclake.yggdrasil.controller

import com.lgou2w.ldk.common.orFalse
import com.lgou2w.mcclake.yggdrasil.YggdrasilLog
import com.lgou2w.mcclake.yggdrasil.dao.Permission
import com.lgou2w.mcclake.yggdrasil.dao.Player
import com.lgou2w.mcclake.yggdrasil.dao.Players
import com.lgou2w.mcclake.yggdrasil.dao.Token
import com.lgou2w.mcclake.yggdrasil.dao.Tokens
import com.lgou2w.mcclake.yggdrasil.dao.User
import com.lgou2w.mcclake.yggdrasil.dao.Users
import com.lgou2w.mcclake.yggdrasil.error.ForbiddenOperationException
import com.lgou2w.mcclake.yggdrasil.security.Email
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.update
import org.joda.time.DateTime
import java.util.*

object AuthController : Controller() {

    const val INVALID_CREDENTIALS_FAILED = "无效认证. 无效邮箱或密码."
    const val INVALID_USER_REGISTERED = "无效认证. 用户已经被注册."
    const val INVALID_USER_NOT_EXISTED = "无效认证. 用户未存在."
    const val INVALID_USER_BANNED = "无效认证. 用户已被封禁."
    const val INVALID_TOKEN_NOT_EXISTED = "无效访问令牌. 未存在."
    const val INVALID_TOKEN_NOT_MATCH = "无效访问令牌. 与客户端令牌不匹配."
    const val INVALID_TOKEN_EXPIRED = "无效访问令牌. 已过期."
    const val INVALID_PROFILE_NOT_EXISTED = "无效档案. 未存在."

    private suspend fun findUserByEmail(email: Email): User? {
        return transaction { User.find { Users.email eq email }.limit(1).firstOrNull() }
    }

    @Throws(ForbiddenOperationException::class)
    private fun User.checkIsNotBanned(): User {
        if (permission == Permission.BANNED)
            throw ForbiddenOperationException(INVALID_USER_BANNED)
        return this
    }

    @Throws(ForbiddenOperationException::class)
    suspend fun register(
            email: String?,
            password: String?,
            nickname: String?,
            permission: Permission = Permission.NORMAL
    ): Map<String, Any?> {
        YggdrasilLog.info("Try to register a new user with : email = $email, password = $password")
        val (email0, password0) = checkIsValidEmailAndPassword(email, password)
        val nickname0 = checkIsValidNickname(nickname)
        val hashedPassword = passwordEncryption.computeHashed(password0)
        val existed = findUserByEmail(email0)
        if (existed != null)
            throw ForbiddenOperationException(INVALID_USER_REGISTERED)
        val user = transaction {
            User.new {
                this.email = email0
                this.password = hashedPassword
                this.nickname = nickname0
                this.permission = permission
            }
        }
        return mapOf(
                "id" to user.id.value,
                "email" to user.email,
                "nickname" to user.nickname,
                "createdAt" to user.createdAt,
                "lastLogged" to user.lastLogged,
                "permission" to user.permission
        )
    }

    @Throws(ForbiddenOperationException::class)
    suspend fun authenticate(
            email: String?,
            password: String?,
            clientToken: String?,
            requestUser: Boolean?
    ): Map<String, Any?> {
        YggdrasilLog.info("User try to authenticate with : email = $email, password = $password, clientToken = $clientToken")
        val (email0, password0) = checkIsValidEmailAndPassword(email, password)
        val clientToken0 = checkIsNonUnsignedUUIDOrNull(clientToken, " 客户端令牌.")
        val user = findUserByEmail(email0)?.checkIsNotBanned()
                   ?: throw ForbiddenOperationException(INVALID_USER_NOT_EXISTED)
        if (!passwordEncryption.compare(password0, user.password))
            throw ForbiddenOperationException(INVALID_CREDENTIALS_FAILED)
        val token = transaction {
            Tokens.deleteWhere { Tokens.user eq user.id }
            Token.new {
                this.clientToken = clientToken0 ?: UUID.randomUUID()
                this.user = user
            }
        }
        YggdrasilLog.info("New access token '${token.id.value}' generated for user : $email0")
        YggdrasilLog.info("User '$email0' authenticated successfully")
        val lastLogged = transaction { DateTime.now().apply { Users.update({ Users.id eq user.id }) { it[lastLogged] = this@apply } } }
        val availableProfiles = transaction { Player.find { Players.user eq user.id }.toList() }
        val response = LinkedHashMap<String, Any>()
        response["accessToken"] = token.id.value
        response["clientToken"] = token.clientToken
        response["availableProfiles"] = availableProfiles.map { it.toProfile() }
        if (availableProfiles.size == 1)
            response["selectedProfile"] = availableProfiles.first().toProfile()
        if (requestUser.orFalse()) {
            response["user"] = mapOf(
                    "id" to user.id.value,
                    "properties" to mapOf(
                            "createdAt" to user.createdAt,
                            "lastLogged" to lastLogged,
                            "permission" to user.permission
                    )
            )
        }
        return response
    }

    @Throws(ForbiddenOperationException::class)
    suspend fun refresh(
            accessToken: String?,
            clientToken: String?,
            requestUser: Boolean?,
            profileId: String?,
            profileName: String?
    ): Map<String, Any?> {
        YggdrasilLog.info("User try to refresh with : accessToken = $accessToken, clientToken = $clientToken")
        val accessToken0 = checkIsNonUnsignedUUID(accessToken, " 访问令牌.")
        val clientToken0 = checkIsNonUnsignedUUIDOrNull(clientToken, " 客户端令牌.")
        val profileId0 = checkIsNonUnsignedUUIDOrNull(profileId, " 档案 Id.")
        val token = transaction { Token.find { Tokens.id eq accessToken0 }.limit(1).firstOrNull() }
                    ?: throw ForbiddenOperationException(INVALID_TOKEN_NOT_EXISTED)
        if (clientToken0 != null && clientToken0 != token.clientToken)
            throw ForbiddenOperationException(INVALID_TOKEN_NOT_MATCH)
        val user = transaction { token.user }.checkIsNotBanned()
        val availableProfiles = transaction { Player.find { Players.user eq user.id }.toList() }
        if (profileId0 != null && availableProfiles.find { it.id.value == profileId0 && it.name == profileName } == null)
            throw ForbiddenOperationException(INVALID_PROFILE_NOT_EXISTED)
        val newToken = transaction {
            val shouldClientToken = clientToken0 ?: token.clientToken
            Tokens.deleteWhere { Tokens.id eq token.id }
            Token.new {
                this.clientToken = shouldClientToken
                this.user = user
            }
        }
        YggdrasilLog.info("New access token '${newToken.id.value}' generated for user : ${user.email}")
        YggdrasilLog.info("Access token refreshed '$accessToken' => '${newToken.id.value}'")
        val response = LinkedHashMap<String, Any>()
        response["accessToken"] = newToken.id.value
        response["clientToken"] = newToken.clientToken
        if (profileId0 == null && availableProfiles.size == 1)
            response["selectedProfile"] = availableProfiles.first().toProfile()
        if (requestUser.orFalse()) {
            response["user"] = mapOf(
                    "id" to user.id.value,
                    "properties" to mapOf(
                            "createdAt" to user.createdAt,
                            "lastLogged" to user.lastLogged,
                            "permission" to user.permission
                    )
            )
        }
        return response
    }

    @Throws(ForbiddenOperationException::class)
    suspend fun validate(
            accessToken: String?,
            clientToken: String?
    ): Boolean {
        YggdrasilLog.info("User try to validate with : accessToken = $accessToken, clientToken = $clientToken")
        val accessToken0 = checkIsNonUnsignedUUID(accessToken, " 访问令牌.")
        val clientToken0 = checkIsNonUnsignedUUIDOrNull(clientToken, " 客户端令牌.")
        val token = transaction { Token.find { Tokens.id eq accessToken0 }.limit(1).firstOrNull() }
                    ?: throw ForbiddenOperationException(INVALID_TOKEN_NOT_EXISTED)
        if (clientToken0 != null && clientToken0 != token.clientToken)
            throw ForbiddenOperationException(INVALID_TOKEN_NOT_MATCH)
        val invalid = token.isInvalid(conf.userTokenInvalidMillis)
        if (invalid) {
            YggdrasilLog.info("Access token '${token.id.value}' has been expired")
            transaction { Tokens.deleteWhere { Tokens.id eq token.id } }
            throw ForbiddenOperationException(INVALID_TOKEN_EXPIRED)
        }
        return !invalid
    }

    @Throws(ForbiddenOperationException::class)
    suspend fun invalidate(
            accessToken: String?,
            clientToken: String?
    ): Boolean {
        YggdrasilLog.info("User try to invalidate with : accessToken = $accessToken, clientToken = $clientToken")
        val accessToken0 = checkIsNonUnsignedUUID(accessToken, " 访问令牌.")
//        val clientToken0 = checkIsNonUnsignedUUIDOrNull(clientToken, " Client token.")
        val token = transaction { Token.find { Tokens.id eq accessToken0 }.limit(1).firstOrNull() }
                    ?: throw ForbiddenOperationException(INVALID_TOKEN_NOT_EXISTED)
        transaction { Tokens.deleteWhere { Tokens.id eq token.id } }
        YggdrasilLog.info("Access token '$accessToken' was successfully revoked")
        return true
    }

    @Throws(ForbiddenOperationException::class)
    suspend fun signout(
            email: String?,
            password: String?
    ): Boolean {
        YggdrasilLog.info("User try to signout with : email = $email, password = $password")
        val (email0, password0) = checkIsValidEmailAndPassword(email, password)
        val user = findUserByEmail(email0)?.checkIsNotBanned()
                   ?: throw ForbiddenOperationException(INVALID_USER_NOT_EXISTED)
        if (!passwordEncryption.compare(password0, user.password))
            throw ForbiddenOperationException(INVALID_CREDENTIALS_FAILED)
        transaction { Tokens.deleteWhere { Tokens.user eq user.id } }
        YggdrasilLog.info("User '${user.email}' signed out, all tokens revoked")
        return true
    }
}

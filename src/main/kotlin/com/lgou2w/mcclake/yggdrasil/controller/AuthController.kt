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
import com.lgou2w.mcclake.yggdrasil.cache.SimpleCleanerMemoryCached
import com.lgou2w.mcclake.yggdrasil.dao.*
import com.lgou2w.mcclake.yggdrasil.email.Templates
import com.lgou2w.mcclake.yggdrasil.error.ForbiddenOperationException
import com.lgou2w.mcclake.yggdrasil.error.InternalServerException
import com.lgou2w.mcclake.yggdrasil.util.Hex
import com.lgou2w.mcclake.yggdrasil.util.UUIDSerializer
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.update
import org.joda.time.DateTime
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

object AuthController : Controller() {

    const val INVALID_CREDENTIALS_FAILED = "无效认证. 无效邮箱或密码."
    const val INVALID_REGISTER_NOT_ALLOWED = "服务器未开启注册."
    const val INVALID_USER_REGISTERED = "无效认证. 用户已经被注册."
    const val INVALID_USER_NOT_EXISTED = "无效认证. 用户未存在."
    const val INVALID_TOKEN_NOT_EXISTED = "无效访问令牌. 未存在."
    const val INVALID_TOKEN_NOT_MATCH = "无效访问令牌. 与客户端令牌不匹配."
    const val INVALID_TOKEN_EXPIRED = "无效访问令牌. 已过期."
    const val INVALID_PROFILE_NOT_EXISTED = "无效档案. 未存在."
    const val INVALID_VERIFY_CODE_NOT_ALLOWED = "服务器验证码功能未开启或不可用."
    const val INVALID_VERIFY_CODE = "无效的验证码."
    const val INVALID_VERIFY_CODE_RULE = "无效的验证码格式."
    const val INVALID_VERIFY_CODE_NOT_MATCH = "无效的验证码. 与预期不符合."

    const val INVALID_K_ACCESS_TOKEN = "访问令牌"
    const val INVALID_K_CLIENT_TOKEN = "客户端令牌"
    const val INVALID_K_PROFILE_ID = "档案 Id"

    const val M_VEIRYF_0 = "向用户 '{}' 发送邮件成功: {}"
    const val M_VERIFY_1 = "向用户 '{}' 发送邮件时错误:"
    const val M_REGISTER_0 = "尝试注册新的用户使用 : 邮箱 = {}, 昵称 = {}, 验证码 = {}" // 密码不暴露给日志
    const val M_REGISTER_1 = "新的用户 '{}' 注册成功"
    const val M_REGISTER_2 = "创建用户昵称 '{}' 的默认玩家 : UUID = {}"
    const val M_REGISTER_3 = "用户验证码 '{}' 和预期不符合, 预期 => '{}'"
    const val M_AUTHENTICATE_0 = "用户尝试 Authenticate 使用 : 邮箱 = {}, 密码 = {}, 客户端令牌 = {}"
    const val M_AUTHENTICATE_1 = "新的访问令牌 '{}' 生成给用户 : {}"
    const val M_AUTHENTICATE_2 = "用户登录成功 : {}"
    const val M_REFRESH_0 = "用户尝试 Refresh 使用 : 访问令牌 = {}, 客户端令牌 = {}"
    const val M_REFRESH_1 = "新的访问令牌 '{}' 生成给用户 : {}"
    const val M_REFRESH_2 = "访问令牌已刷新 '{}' => '{}'"
    const val M_VALIDATE_0 = "用户尝试 Validate 使用 : 访问令牌 = {}, 客户端令牌 = {}"
    const val M_VALIDATE_1 = "访问令牌 '{}' 已过期"
    const val M_INVALIDATE_0 = "用户尝试 Invalidate 使用 : 访问令牌 = {}, 客户端令牌 = {}"
    const val M_INVALIDATE_1 = "访问令牌 '{}' 已成功删除"
    const val M_SIGNOUT_0 = "用户尝试 Signout 使用 : 邮箱 = {}"
    const val M_SIGNOUT_1 = "用户 '{}' 登出, 访问令牌已全部删除"

    private val verifyCodeTimeoutDefault = 300L
    private val verifyCodeCached by lazy {
        var timeout = conf.userVerifyCodeTimeout
        var length = conf.userVerifyCodeLength
        if (timeout <= 0) {
            timeout = verifyCodeTimeoutDefault
            YggdrasilLog.warn("警告: 验证码超时时间必须大于 0, 将使用默认 300 秒")
        }
        if (length <= 0) {
            length = Hex.HEX
            YggdrasilLog.warn("警告: 验证码随机长度必须大于 0, 将使用默认 16 位")
        }
        object : SimpleCleanerMemoryCached<String, String>(
                // 第一次执行清理的等待时间，默认超时时间
                timeout,
                // 每次间隔的时间，默认超时时间的 1.5 倍
                // 假设 5 分钟超时，那么每隔 7.5 分钟清理一次
                Math.round(timeout * 1.5),
                TimeUnit.SECONDS, "verifycode"
        ) {
            private val codeTimeout = timeout * 1000L // 秒到毫秒
            private val codeLength = length
            private val counter = AtomicInteger(0)

            // 延时开启清洁器线程
            // 默认应用程序启动后不 start
            // 当生成后并且超过指定阈值
            // 那么开始清洁功能
            private val threshold = Runtime.getRuntime().availableProcessors() * 10
            private fun canStartWork() {
                if (!isAtWorking && counter.incrementAndGet() > threshold) {
                    YggdrasilLog.info("验证码缓存清洁器已开始工作...")
                    YggdrasilLog.info("目前已缓存的验证码数量 : $size")
                    start()
                    counter.set(0)
                    setOnRemoved { code ->
                        YggdrasilLog.info("用户 '{}' 的验证码已过期并移除 : {}", code.first, code.second)
                    }
                } else if (isAtWorking && size * 2 < threshold) {
                    YggdrasilLog.info("验证码缓存清洁器已暂时停止...")
                    close()
                }
            }

            // 生成验证码并加入缓存
            fun generate(key: String): String {
                canStartWork()
                val code = Hex.generate(codeLength)
                put(key, code, codeTimeout)
                YggdrasilLog.info("给用户 '{}' 生成一个新的验证码 : {}", key, code)
                return code
            }

            // 获取验证码，如果验证码已过期，那么会自动移除
            override fun get(key: String): String? {
                val cached = super.getCached(key)
                if (cached != null && cached.isExpired) {
                    YggdrasilLog.info("用户 '{}' 的验证码已过期并移除 : {}", key, cached.data)
                    remove(key)
                    return null
                }
                return cached?.data
            }
        }
    }

    @Throws(ForbiddenOperationException::class)
    suspend fun verify(
            email: String?,
            nickname: String?
    ): Map<String, Any?> {

        // TODO 到时候可能会有忘记密码时的验证码，这个地方等待确认是否修改
        // 配置未开启注册功能, 返回 403 禁止操作
        if (!conf.userRegistrationEnable)
            throw ForbiddenOperationException(INVALID_REGISTER_NOT_ALLOWED)

        // 配置未开启验证码功能，返回 403 禁止操作
        // 或者邮箱管理器信使不可用，也返回
        if (!conf.userVerifyCodeEnable || !emailManager.isAvailable)
            throw ForbiddenOperationException(INVALID_VERIFY_CODE_NOT_ALLOWED)

        val email0 = checkIsValidEmail(email)
        val nickname0 = checkIsValidNickname(nickname)

        // 先从缓存获取，如果缓存没有
        // 那么给用户重新生成验证码并发送邮件
        // 否则直接响应缓存验证码内容，不需要重新发送邮件
        var verifyCode = verifyCodeCached[email0.value]
        if (verifyCode == null) {
            verifyCode = verifyCodeCached.generate(email0.value)
            try {
                val template = Templates.parse(workDir, Templates.T_REGISTER, true,
                        "%nickname%" to nickname0,
                        "%email%" to email0.value,
                        "%verifyCode%" to verifyCode
                )
                // 阻塞响应直到发送成功或失败
                val emailResponse = emailManager.sendHtml(email0.value, template.subject, template.content)
                YggdrasilLog.info(M_VEIRYF_0, emailResponse)
            } catch (e: Exception) {
                // 解析模板发送邮件失败，移除验证码缓存并抛出服务器内部异常
                verifyCodeCached.remove(email0.value)
                YggdrasilLog.error(M_VERIFY_1, e, email0.value)
                throw InternalServerException()
            }
        }

        return mapOf(
                "email" to email0.value,
                "nickname" to nickname0,
                "verifyCode" to verifyCode
        )
    }

    @Throws(ForbiddenOperationException::class)
    suspend fun register(
            email: String?,
            password: String?,
            nickname: String?,
            verifyCode: String?,
            permission: Permission = Permission.NORMAL
    ): Map<String, Any?> {

        // 配置未开启注册功能, 返回 403 禁止操作
        if (!conf.userRegistrationEnable)
            throw ForbiddenOperationException(INVALID_REGISTER_NOT_ALLOWED)

        YggdrasilLog.info(M_REGISTER_0, email, nickname, verifyCode)

        val (email0, password0) = checkIsValidEmailAndPassword(email, password)
        val nickname0 = checkIsValidNickname(nickname)

        // 检验是否开启验证码功能
        if (conf.userVerifyCodeEnable) {
            if (verifyCode == null)
                throw ForbiddenOperationException(INVALID_VERIFY_CODE)
            if (!Hex.isMatches(verifyCode, allowBlank = false))
                throw ForbiddenOperationException(INVALID_VERIFY_CODE_RULE)

            // 用户提交的验证码和缓存的验证码不匹配，禁止注册
            val code = verifyCodeCached[email0.value]
            if (verifyCode != code) {
                YggdrasilLog.info(M_REGISTER_3, verifyCode, code)
                throw ForbiddenOperationException(INVALID_VERIFY_CODE_NOT_MATCH)
            }
        }

        val hashedPassword = passwordEncryption.computeHashed(password0)
        val existed = findUserByEmailOrNickname(email0, nickname0)
        if (existed != null)
            throw ForbiddenOperationException(INVALID_USER_REGISTERED)

        val user = transaction {
            val user = User.new {
                this.email = email0
                this.password = hashedPassword
                this.nickname = nickname0
                this.permission = permission
            }
            YggdrasilLog.info(M_REGISTER_1, user.email.value)
            if (conf.userRegistrationNicknamePlayer) {
                val player = Player.new {
                    this.name = nickname0
                    this.user = user
                }
                YggdrasilLog.info(M_REGISTER_2, user.nickname, UUIDSerializer.fromUUID(player.id.value))
            }
            user
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

        YggdrasilLog.info(M_AUTHENTICATE_0, email, password, clientToken)

        val (email0, password0) = checkIsValidEmailAndPassword(email, password)
        val clientToken0 = checkIsNonUnsignedUUIDOrNull(clientToken, INVALID_K_CLIENT_TOKEN)

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
        YggdrasilLog.info(M_AUTHENTICATE_1, token.id.value, email0.value)
        YggdrasilLog.info(M_AUTHENTICATE_2, email0.value)

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

        YggdrasilLog.info(M_REFRESH_0, accessToken, clientToken)

        val accessToken0 = checkIsNonUnsignedUUID(accessToken, INVALID_K_ACCESS_TOKEN)
        val clientToken0 = checkIsNonUnsignedUUIDOrNull(clientToken, INVALID_K_CLIENT_TOKEN)
        val profileId0 = checkIsNonUnsignedUUIDOrNull(profileId, INVALID_K_PROFILE_ID)

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

        YggdrasilLog.info(M_REFRESH_1, token.id.value, user.email.value)
        YggdrasilLog.info(M_REFRESH_2, accessToken, newToken.id.value)

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

        YggdrasilLog.info(M_VALIDATE_0, accessToken, clientToken)

        val accessToken0 = checkIsNonUnsignedUUID(accessToken, INVALID_K_ACCESS_TOKEN)
        val clientToken0 = checkIsNonUnsignedUUIDOrNull(clientToken, INVALID_K_CLIENT_TOKEN)

        val token = transaction { Token.find { Tokens.id eq accessToken0 }.limit(1).firstOrNull() }
                ?: throw ForbiddenOperationException(INVALID_TOKEN_NOT_EXISTED)
        if (clientToken0 != null && clientToken0 != token.clientToken)
            throw ForbiddenOperationException(INVALID_TOKEN_NOT_MATCH)

        val invalid = token.isInvalid(conf.userTokenInvalidMillis)
        if (invalid) {
            YggdrasilLog.info(M_VALIDATE_1, accessToken)
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

        YggdrasilLog.info(M_INVALIDATE_0, accessToken, clientToken)

        val accessToken0 = checkIsNonUnsignedUUID(accessToken, INVALID_K_ACCESS_TOKEN)
//        val clientToken0 = checkIsNonUnsignedUUIDOrNull(clientToken, INVALID_K_CLIENT_TOKEN)

        val token = transaction { Token.find { Tokens.id eq accessToken0 }.limit(1).firstOrNull() }
                ?: throw ForbiddenOperationException(INVALID_TOKEN_NOT_EXISTED)

        transaction { Tokens.deleteWhere { Tokens.id eq token.id } }
        YggdrasilLog.info(M_INVALIDATE_1, accessToken)
        return true
    }

    @Throws(ForbiddenOperationException::class)
    suspend fun signout(
            email: String?,
            password: String?
    ): Boolean {

        YggdrasilLog.info(M_SIGNOUT_0, email)

        val (email0, password0) = checkIsValidEmailAndPassword(email, password)

        val user = findUserByEmail(email0)?.checkIsNotBanned()
                ?: throw ForbiddenOperationException(INVALID_USER_NOT_EXISTED)
        if (!passwordEncryption.compare(password0, user.password))
            throw ForbiddenOperationException(INVALID_CREDENTIALS_FAILED)

        transaction { Tokens.deleteWhere { Tokens.user eq user.id } }
        YggdrasilLog.info(M_SIGNOUT_1, user.email.value)
        return true
    }
}

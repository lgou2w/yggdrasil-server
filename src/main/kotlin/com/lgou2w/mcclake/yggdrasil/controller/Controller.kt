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

import com.lgou2w.mcclake.yggdrasil.DefaultYggdrasilService
import com.lgou2w.mcclake.yggdrasil.YggdrasilService
import com.lgou2w.mcclake.yggdrasil.error.ForbiddenOperationException
import com.lgou2w.mcclake.yggdrasil.security.Email
import com.lgou2w.mcclake.yggdrasil.security.Emails
import com.lgou2w.mcclake.yggdrasil.util.UUIDSerializer
import java.util.*

abstract class Controller : YggdrasilService by DefaultYggdrasilService {

    companion object {
        const val INVALID_NON_UNSINGED_UUID = "非无符号 UUID 格式."
        const val INVALID_EMAIL_FORMAT = "无效的邮箱格式."
        const val INVALID_PASSWORD_FORMAT = "无效的密码格式."
        const val INVALID_PASSWORD_FORMAT_RULE = "无效的密码格式. 规则: "
        const val INVALID_PASSWORD_FORMAT_RULE2 = "无效的密码格式. 无效强度. 规则: "
        const val INVALID_NICKNAME_FORMAT = "无效的昵称格式."
        const val INVALID_NICKNAME_FORMAT_RULE = "无效昵称格式. 规则: "
    }

    @Throws(ForbiddenOperationException::class)
    fun checkIsNonUnsignedUUID(uuid: String?, cause: String): UUID {
        return UUIDSerializer.fromStringSafe(uuid) ?:
               throw ForbiddenOperationException(INVALID_NON_UNSINGED_UUID + cause)
    }

    @Throws(ForbiddenOperationException::class)
    fun checkIsNonUnsignedUUIDOrNull(uuid: String?, cause: String): UUID? {
        if (uuid == null) return null
        return checkIsNonUnsignedUUID(uuid, cause)
    }

    @Throws(ForbiddenOperationException::class)
    fun checkIsValidEmail(email: String?): Email {
        return Emails.parseSafely(email)
               ?: throw ForbiddenOperationException(INVALID_EMAIL_FORMAT)
    }

    @Throws(ForbiddenOperationException::class)
    fun checkIsValidEmailAndPassword(email: String?, password: String?): Pair<Email, String> {
        val email0 = checkIsValidEmail(email)
        if (password == null)
            throw ForbiddenOperationException(INVALID_PASSWORD_FORMAT)
        if (!conf.userRegistrationPasswordVerify.matcher(password).matches())
            throw ForbiddenOperationException(INVALID_PASSWORD_FORMAT_RULE + conf.userRegistrationPasswordVerify.pattern())
        if (!conf.userRegistrationPasswordStrengthVerify.matcher(password).matches())
            throw ForbiddenOperationException(INVALID_PASSWORD_FORMAT_RULE2 + conf.userRegistrationPasswordStrengthVerify.pattern())
        return email0 to password
    }

    @Throws(ForbiddenOperationException::class)
    fun checkIsValidNickname(nickname: String?): String {
        if (nickname == null)
            throw ForbiddenOperationException(INVALID_NICKNAME_FORMAT)
        if (!conf.userRegistrationNicknameVerify.matcher(nickname).matches())
            throw ForbiddenOperationException(INVALID_NICKNAME_FORMAT_RULE + conf.userRegistrationNicknameVerify.pattern())
        return nickname
    }
}

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

package com.lgou2w.mcclake.yggdrasil.email

import com.lgou2w.mcclake.yggdrasil.YggdrasilConf
import com.lgou2w.mcclake.yggdrasil.YggdrasilLog
import java.io.Closeable

class EmailManager(
        private val conf: YggdrasilConf
) : Closeable {

    companion object {
        const val MESSAGER_DEFAULT = "default"
        const val MESSAGER_SENDGRID = "sendgrid"
        const val M_INITIALIZE_ERROR = "错误, 初始化邮件信使时异常:"
        const val M_INVALID = "邮件信徒不可用"
        const val M_USE_TYPE = "使用邮件信徒方案类型 : {}"
    }

    val from = conf.messagerFrom
    val messager : Messager? = try {
        YggdrasilLog.info(M_USE_TYPE, conf.messagerType)
        when (conf.messagerType?.toLowerCase()) {
            MESSAGER_DEFAULT -> MessagerDefault()
            MESSAGER_SENDGRID -> MessagerSendGrid()
            else -> null
        }.apply {
            this?.initialize(conf)
        }
    } catch (e: Exception) {
        YggdrasilLog.error(M_INITIALIZE_ERROR, e)
        YggdrasilLog.error(M_INVALID)
        null
    }

    /**
     * @throws [UnsupportedOperationException]
     */
    val messagerUnsafe : Messager
        get() = messager ?: throw UnsupportedOperationException(M_INVALID)

    @Throws(UnsupportedOperationException::class)
    suspend fun send(to: String, subject: String, content: String): Messager.Response
            = messagerUnsafe.dispatch(listOf(to), subject, content, false)
    @Throws(UnsupportedOperationException::class)
    suspend fun sendHtml(to: String, subject: String, content: String): Messager.Response
            = messagerUnsafe.dispatch(listOf(to), subject, content, true)

    override fun close() {
        messager?.close()
    }
}

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
import org.apache.commons.mail.HtmlEmail

class MessagerDefault : Messager() {

    private lateinit var pwd : String
    private lateinit var smtp : String
    private var port = 465
    private var ssl = true

    override fun initialize(conf: YggdrasilConf) {
        super.initialize(conf)
        this.smtp = conf.messagerDefaultSmtp
        this.port = conf.messagerDefaultPort
        this.ssl = conf.messagerDefaultSsl
        this.pwd = conf.messagerDefaultPwd
        if (pwd.isBlank())
            throw IllegalArgumentException("错误, 发件人邮箱密码不能为空.")
        YggdrasilLog.info("= 使用邮件 SMTP 服务器 : $smtp")
        YggdrasilLog.info("= 使用邮件 SMTP 服务器端口 : $port")
        YggdrasilLog.info("= 是否使用邮件 SSL 服务器 : $ssl")
    }

    data class ResponseDefault(val messageId: String?) : Response

    override suspend fun dispatch(
            to: List<String>,
            subject: String,
            content: String,
            isHtml: Boolean
    ): ResponseDefault {
        return cf.with {
            val email = HtmlEmail().apply { // 总是使用 Html 邮件
                this.setCharset("UTF-8")
                this.hostName = smtp
                this.sslSmtpPort = port.toString()
                this.isSSLOnConnect = ssl
                this.setAuthentication(from, pwd)
                this.setFrom(from)
                this.subject = subject
                this.addTo(*to.toTypedArray())
                if (isHtml) setHtmlMsg(content)
                else setMsg(content)
            }
            val messageId = email.send()
            ResponseDefault(messageId)
        }
    }
}

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
import com.sendgrid.*
import com.sendgrid.Email

class MessagerSendGrid : Messager() {

    companion object {
        private const val TYPE_PLAIN = "text/plain"
        private const val TYPE_HTML = "text/html"
        private const val END_POINT_SEND = "mail/send"
        private const val M_INVALID_API_KEY = "错误, 无效的 SendGrid API 密钥 : "
    }

    private lateinit var apiKey : String

    override fun initialize(conf: YggdrasilConf) {
        super.initialize(conf)
        val apiKey = conf.messagerSendGridApiKey
        if (apiKey == null || apiKey.isBlank())
            throw IllegalArgumentException(M_INVALID_API_KEY + apiKey)
        this.apiKey = apiKey
    }

    data class ResponseSendGrid(
            val statusCode: Int?,
            val body: String?,
            val headers: Map<String, String>?
    ) : Response

    override suspend fun dispatch(
            to: List<String>,
            subject: String,
            content: String,
            isHtml: Boolean
    ): ResponseSendGrid {
        return cf.with {
            val fromEmail = Email(from)
            val contentBody = Content(if (isHtml) TYPE_HTML else TYPE_PLAIN, content)
            val mail = Mail().apply {
                this.from = fromEmail
                this.subject = subject
                this.addContent(contentBody)
                this.addPersonalization(Personalization().apply { to.forEach { addTo(Email(it)) } })
            }
            val sg = SendGrid(apiKey)
            val req = Request().apply {
                this.method = Method.POST
                this.endpoint = END_POINT_SEND
                this.body = mail.build()
            }
            val res = sg.api(req)
            ResponseSendGrid(
                    res.statusCode,
                    res.body,
                    res.headers
            )
        }
    }
}

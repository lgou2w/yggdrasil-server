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

package com.lgou2w.mcclake.yggdrasil.util

import com.sendgrid.*
import com.sendgrid.SendGrid

data class MailResponse(
        val statusCode: Int,
        val body: String,
        val headers: Map<String, String>
)

object SendGrid {

    private const val TYPE_PLAIN = "text/plain"
    private const val TYPE_HTML = "text/html"

    fun sendMail(apiKey: String, from: String, subject: String, to: String, content: String): MailResponse
            = sendMail(apiKey, from, subject, to, TYPE_PLAIN, content)
    fun sendMailOfHtml(apiKey: String, from: String, subject: String, to: String, content: String): MailResponse
            = sendMail(apiKey, from, subject, to, TYPE_HTML, content)

    private fun sendMail(apiKey: String, from: String, subject: String, to: String, contentType: String, content: String): MailResponse {
        val fromEmail = Email(from)
        val toEmail = Email(to)
        val contentBody = Content(contentType, content)
        val mail = Mail(fromEmail, subject, toEmail, contentBody)
        val sg = SendGrid(apiKey)
        val req = Request()
        req.method = Method.POST
        req.endpoint = "mail/send"
        req.body = mail.build()
        val res = sg.api(req)
        return MailResponse(
                res.statusCode,
                res.body,
                res.headers
        )
    }
}

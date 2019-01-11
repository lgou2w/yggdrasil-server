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

package com.lgou2w.mcclake.yggdrasil

import com.lgou2w.mcclake.yggdrasil.security.Emails
import org.apache.commons.mail.SimpleEmail
import org.junit.Assert
import org.junit.Ignore
import org.junit.Test

class EmailTest {

    @Test
    fun testEmailParse() {
        val str = "lgou2w@hotmail.com"
        val email = Emails.parse(str)
        Assert.assertEquals("lgou2w", email.id)
        Assert.assertEquals("hotmail.com", email.domain)
    }

    @Test
    fun testEmailNot() {
        val error = "a@bc.def" // error, id at least 3 character
        Emails.parse(error)
    }

    @Test
    fun testEmailSupported() {
        val e1 = "abc456@email.com"
        val e2 = "abc456@email.com.cn"
        val e3 = "abc456.it@email.com"
        val e4 = "abc456_-.@email.com"
        val list = listOf(e1, e2, e3, e4)
        val emails = list.map { Emails.parse(it) }
        Assert.assertEquals(list, emails.map { it.full })
    }

    @Test
    @Ignore
    fun testEmailSend() {
        val email = SimpleEmail()
        email.hostName = "smtp.qq.com"
        email.sslSmtpPort = "465"
        email.isSSLOnConnect = true
        email.setAuthentication("lgou2w@vip.qq.com", "qq 邮箱授权码，或者其他邮箱服务器令牌或密码")
        email.setFrom("lgou2w@vip.qq.com", "lgou2w")
        email.addTo("lgou2w@vip.qq.com")
        email.subject = "欢迎来到像素时光"
        email.setMsg("新用户 大白 的注册验证码是: abcdefg123")
        println(email.send())
    }
}

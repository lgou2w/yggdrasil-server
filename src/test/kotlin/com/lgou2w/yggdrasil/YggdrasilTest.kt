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

package com.lgou2w.yggdrasil

import com.lgou2w.yggdrasil.email.Templates
import com.typesafe.config.ConfigFactory
import org.joda.time.DateTime
import org.joda.time.Period
import org.joda.time.PeriodType
import org.junit.Assert
import org.junit.Ignore
import org.junit.Test
import java.util.regex.Pattern

class YggdrasilTest {

    @Test
    @Ignore
    fun testConf() {
        val config = ConfigFactory.parseResources("yggdrasil.conf")
        val mysql = config.getConfig("yggdrasil.storage.mysql")
        mysql.entrySet().forEach { println(it) }
    }

    @Test
    fun testEmailDefault() {
        val pattern = Pattern.compile("^([A-Za-z0-9_\\-]{3,})@(([A-Za-z0-9_\\-]+)\\.([A-Za-z]{2,})(\\.([A-Za-z]{2,}))?)$")
        val supportedEmails = listOf(
                "lgou2w@hotmai.com",
                "hello-world@java.com.cn",
                "l-_-_-_-_-123abc@vip.qq.com",
                "admin@soulbound.me",
                "root@myblog.online.cn"
        )
        Assert.assertEquals(true, supportedEmails
            .all { pattern.matcher(it).matches() })

        val unsupportedEmails = listOf(
                "a@a.c", // 前：少于 3 位，后：域名后缀少于 2 位
                "b.ca12@blog.com", // 前，出现 . 不允许字符
                "my@blog.online.com.cn" // 后，三次域名，最大支持二级
        )
        Assert.assertEquals(false, unsupportedEmails
            .all { pattern.matcher(it).matches() })
    }

    @Test
    @Ignore
    fun testTemplate() {
        val template = Templates.parse("yggdrasil-template-register.conf",
                "%nickname%" to "lgou2w",
                "%email%" to "lgou2w@hotmail.com",
                "%verifyCode%" to 123
        )
        println(template.subject)
        println(template.content)
    }
}

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
import org.junit.Assert
import org.junit.Test

class EmailTest {

    @Test
    fun testEmailParse() {
        val str = "lgou2w@hotmail.com"
        val email = Emails.parse(str)
        Assert.assertEquals("lgou2w", email.id)
        Assert.assertEquals("hotmail.com", email.domain)
    }

    @Test(expected = UnsupportedOperationException::class)
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
}

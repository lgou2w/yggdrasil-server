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

import com.lgou2w.mcclake.yggdrasil.security.SaltedSha256PasswordEncryption
import org.junit.Assert
import org.junit.Test
import java.util.regex.Pattern

class PasswordTest {

    @Test
    fun testPasswordEncryption() {
        val encryption = SaltedSha256PasswordEncryption()
        val raw = "123456"
        val hashed = encryption.computeHashed(raw)
        println("Hash : " + hashed.hash)
        println("Salt : " + hashed.salt)
        Assert.assertTrue(encryption.compare(raw, hashed))
    }

    @Test
    fun testPasswordStrength() {
        val pattern = Pattern.compile("^(?=.*?[A-Za-z])(?=.*?[0-9]).{8,}\$")
        val pwd1 = "123456"
        val pwd2 = "123456789"
        val pwd3 = "123456789aBc"
        Assert.assertEquals(false, pattern.matcher(pwd1).matches())
        Assert.assertEquals(false, pattern.matcher(pwd2).matches())
        Assert.assertEquals(true, pattern.matcher(pwd3).matches())
    }
}

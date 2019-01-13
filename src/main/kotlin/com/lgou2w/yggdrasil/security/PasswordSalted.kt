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

package com.lgou2w.yggdrasil.security

import com.lgou2w.yggdrasil.util.Hash
import com.lgou2w.yggdrasil.util.Hex

abstract class SaltedHashPasswordEncryption : HexSaltedPasswordEncryption() {

    abstract val algorithm : String

    final override val saltLength: Int = Hex.HEX

    final override fun parse(encrypted: String): HashedPassword {
        val array = encrypted.split("\$")
        return if (array.size == 4) {
            val salt = array[2]
            HashedPassword(encrypted, salt)
        } else {
            throw IllegalArgumentException(
                    "The encrypted data to be parsed may not match this encryption algorithm.")
        }
    }

    final override fun computeHash(password: String, salt: String?): String {
        val hash = Hash.digest(algorithm, Hash.digest(algorithm, password) + salt)
        return "\$HASH\$$salt\$$hash"
    }

    final override fun compare(password: String, hashed: HashedPassword): Boolean {
        val hash = hashed.hash
        val array = hash.split("\$")
        return if (array.size == 4) {
            val salt = array[2]
            hash == computeHash(password, salt)
        } else {
            false
        }
    }
}

class SaltedMd5PasswordEncryption : SaltedHashPasswordEncryption() {

    override val algorithm: String = Hash.MD5
}

class SaltedSha1PasswordEncryption : SaltedHashPasswordEncryption() {

    override val algorithm: String = Hash.SHA1
}

class SaltedSha256PasswordEncryption : SaltedHashPasswordEncryption() {

    override val algorithm: String = Hash.SHA256
}

class SaltedSha512PasswordEncryption : SaltedHashPasswordEncryption() {

    override val algorithm: String = Hash.SHA512
}

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

package com.lgou2w.mcclake.yggdrasil.security

import com.lgou2w.mcclake.yggdrasil.util.Hash

abstract class UnsaltedPasswordEncryption : PasswordEncryption {

    final override fun parse(encrypted: String): HashedPassword {
        return HashedPassword(encrypted, null)
    }

    final override fun computeHashed(password: String): HashedPassword {
        return HashedPassword(computeHash(password, null), null)
    }

    final override fun compare(password: String, hashed: HashedPassword): Boolean {
        return hashed.hash == password
    }

    final override fun generateSalt(): String? {
        return null
    }

    final override val hasSeparateSalt: Boolean = false
}

@Deprecated("Unsafe")
class PlainTextPasswordEncryption : UnsaltedPasswordEncryption() {

    override fun computeHash(password: String, salt: String?): String {
        return password
    }
}

@Deprecated("Unsafe")
class Md5PasswordEncryption : UnsaltedPasswordEncryption() {

    override fun computeHash(password: String, salt: String?): String {
        return Hash.digest(Hash.MD5, password)
    }
}

@Deprecated("Unsafe")
class Sha1PasswordEncryption : UnsaltedPasswordEncryption() {

    override fun computeHash(password: String, salt: String?): String {
        return Hash.digest(Hash.SHA1, password)
    }
}

class Sha256PasswordEncryption : UnsaltedPasswordEncryption() {

    override fun computeHash(password: String, salt: String?): String {
        return Hash.digest(Hash.SHA256, password)
    }
}

class Sha512PasswordEncryption : UnsaltedPasswordEncryption() {

    override fun computeHash(password: String, salt: String?): String {
        return Hash.digest(Hash.SHA512, password)
    }
}

/*
 * Copyright (C) 2019 The lgou2w <lgou2w@hotmail.com>
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

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.lgou2w.ldk.common.notNull
import com.lgou2w.yggdrasil.util.Hex
import java.lang.reflect.Type
import java.util.Collections
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

data class HashedPassword(val hash: String, val salt: String? = null)

interface PasswordEncryption {

    @Throws(IllegalArgumentException::class)
    fun parse(encrypted: String): HashedPassword

    fun computeHash(password: String, salt: String?): String

    fun computeHashed(password: String): HashedPassword

    fun compare(password: String, hashed: HashedPassword): Boolean

    fun generateSalt(): String?

    val hasSeparateSalt : Boolean
}

abstract class HexSaltedPasswordEncryption : PasswordEncryption {

    abstract val saltLength : Int

    final override fun computeHashed(password: String): HashedPassword {
        val salt = generateSalt()
        return HashedPassword(computeHash(password, salt), salt)
    }

    final override fun generateSalt(): String = Hex.generate(saltLength)

    final override val hasSeparateSalt: Boolean = true
}

class HashedPasswordSerializer(
        private val passwordEncryption: PasswordEncryption
) : JsonSerializer<HashedPassword>, JsonDeserializer<HashedPassword> {
    override fun serialize(src: HashedPassword?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement? {
        if (src == null) return null
        return JsonPrimitive(src.hash)
    }
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): HashedPassword? {
        if (json == null || json !is JsonPrimitive) return null
        return passwordEncryption.parse(json.asString)
    }
}

object Passwords {

    @Suppress("DEPRECATION")
    val supportedEncryption: Map<String, KClass<out PasswordEncryption>> = Collections.unmodifiableMap(mapOf(
            // Deprecated and unsecure password encryption
            "Raw" to PlainTextPasswordEncryption::class,
            "Md5" to Md5PasswordEncryption::class,
            "Sha1" to Sha1PasswordEncryption::class,
            // Recommended password encryption
            "Sha256" to Sha256PasswordEncryption::class,
            "Sha512" to Sha512PasswordEncryption::class,
            "SaltedMd5" to SaltedMd5PasswordEncryption::class,
            "SaltedSha1" to SaltedSha1PasswordEncryption::class,
            "SaltedSha256" to SaltedSha256PasswordEncryption::class,
            "SaltedSha512" to SaltedSha512PasswordEncryption::class
    )).notNull()

    @Throws(IllegalArgumentException::class)
    fun newEncryption(type: String): PasswordEncryption {
        val lookup = supportedEncryption[type]
                     ?: throw IllegalArgumentException("Unsupported password encryption type: $type")
        return lookup.createInstance()
    }
}

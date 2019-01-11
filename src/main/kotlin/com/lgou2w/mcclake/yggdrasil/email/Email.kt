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

import com.google.gson.*
import java.lang.reflect.Type
import java.util.regex.Pattern

data class Email(val id: String, val domain: String) {
    val full = "$id@$domain"
    override fun toString(): String = full
}

object EmailSerializer : JsonSerializer<Email>, JsonDeserializer<Email> {
    override fun serialize(src: Email?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement? {
        if (src == null) return null
        return JsonPrimitive(src.full)
    }
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Email? {
        if (json == null || json !is JsonPrimitive) return null
        return Emails.parse(json.asString)
    }
}

object Emails {

    // TODO 自定义验证

    val PATTERN = Pattern.compile("^(?<id>[A-Za-z0-9_\\-.]{3,})@(?<domain>([A-Za-z0-9_\\-.])+\\.([A-Za-z]{2,4}))$")

    @Throws(UnsupportedOperationException::class)
    fun parse(email: String?): Email {
        if (email == null) throw NullPointerException("email")
        val matcher = PATTERN.matcher(email)
        if (matcher.matches()) {
            val id = matcher.group("id")
            val domain = matcher.group("domain")
            return Email(id, domain)
        }
        throw UnsupportedOperationException(
                "Unsupported email format : $email")
    }

    fun parseSafely(email: String?): Email? {
        if (email == null) return null
        return try {
            parse(email)
        } catch (e: UnsupportedOperationException) {
            null
        }
    }
}

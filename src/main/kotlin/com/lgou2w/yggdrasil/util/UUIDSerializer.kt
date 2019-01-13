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

package com.lgou2w.yggdrasil.util

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.util.*

object UUIDSerializer : TypeAdapter<UUID>() {

    override fun write(writer: JsonWriter, value: UUID?) {
        writer.value(fromUUID(value))
    }

    override fun read(reader: JsonReader): UUID? {
        return fromString(reader.nextString())
    }

    fun fromUUID(value: UUID?): String
            = value?.toString()?.replace("-", "") ?: ""

    fun fromString(value: String?): UUID? {
        if (value == null || value == "")
            return null
        return UUID.fromString(
                value.replace(Regex("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})"), "\$1-\$2-\$3-\$4-\$5")
        )
    }

    fun fromStringSafe(value: String?): UUID? = try {
        fromString(value)
    } catch (e: IllegalArgumentException) {
        null
    }
}

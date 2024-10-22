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

package com.lgou2w.yggdrasil.util

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import org.joda.time.DateTime

object DateTimeSerializer : TypeAdapter<DateTime>() {

    override fun write(writer: JsonWriter, value: DateTime) {
        writer.value(toTimestamp(value))
    }

    override fun read(reader: JsonReader): DateTime {
        return fromTimestamp(reader.nextLong())
    }

    fun fromMilliseconds(milliseconds: Long): DateTime
            = DateTime(milliseconds)

    fun fromTimestamp(timestamp: Long): DateTime
            = DateTime(timestamp * 1000L)

    fun toMilliseconds(value: DateTime): Long
            = value.millis

    fun toTimestamp(value: DateTime): Long
            = value.millis / 1000L
}

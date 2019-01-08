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

package com.lgou2w.mcclake.yggdrasil.dao

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.lgou2w.ldk.common.Enums
import com.lgou2w.ldk.common.Valuable
import com.lgou2w.mcclake.yggdrasil.DefaultYggdrasilService
import com.lgou2w.mcclake.yggdrasil.security.Email
import com.lgou2w.mcclake.yggdrasil.security.HashedPassword
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.joda.time.DateTime
import java.lang.reflect.Type
import java.util.*

object Users : Dao.UnsignedUUIDTable("yggdrasil_users", "uuid") {
    var email = registerColumn<Email>("email", Dao.EmailColumnType()).uniqueIndex()
    var password = registerColumn<HashedPassword>("password",
            Dao.HashedPasswordColumnType(DefaultYggdrasilService.passwordEncryption))
    var nickname = varchar("nickname", 64).uniqueIndex().nullable()
    var createdAt = datetime("createdAt").clientDefault { DateTime.now() }
    var lastLogged = datetime("lastLogged").clientDefault { DateTime.now() }
    var permission = registerColumn<Permission>("permission", Dao.PermissionColumnType())
        .default(Permission.NORMAL)
}

class User(
        id: EntityID<UUID>
) : UUIDEntity(id) {
    companion object : UUIDEntityClass<User>(Users)
    var email by Users.email
    var password by Users.password
    var nickname by Users.nickname
    var createdAt by Users.createdAt
    var lastLogged by Users.lastLogged
    var permission by Users.permission
}

enum class Permission(
        override val value: Int
) : Valuable<Int> {
    BANNED(-1),
    NORMAL(0),
    ADMIN(1),
    OWNER(2),
    ;
}

object PermissionSerializer : JsonSerializer<Permission>, JsonDeserializer<Permission> {
    override fun serialize(src: Permission?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement? {
        if (src == null) return null
        return JsonPrimitive(src.value)
    }
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Permission? {
        if (json == null || json !is JsonPrimitive) return null
        return Enums.ofValuableNotNull(Permission::class.java, json.asInt)
    }
}

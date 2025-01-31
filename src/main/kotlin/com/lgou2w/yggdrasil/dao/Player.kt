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

package com.lgou2w.yggdrasil.dao

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import java.util.UUID

object Players : Dao.UnsignedUUIDTable("yggdrasil_players", "uuid") {
    var name = varchar("name", 16).uniqueIndex()
    var model = enumerationByName("model", 16, ModelType::class).default(ModelType.STEVE)
    var user = reference("user", Users)
}

class Player(
        id: EntityID<UUID>
) : UUIDEntity(id) {
    companion object : UUIDEntityClass<Player>(Players)
    var name by Players.name
    var user by User referencedOn Players.user
    var model by Players.model

    fun toProfile(): Map<String, Any>
            = mapOf("id" to id.value, "name" to name)
}

enum class ModelType {
    STEVE,
    ALEX,
    ;
}

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
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

object Textures : IntIdTable("yggdrasil_textures", "id") {
    var type = enumerationByName("type", 16, TextureType::class)
    var url = text("url")
    var player = reference("player", Players)
}

class Texture(
        id: EntityID<Int>
) : IntEntity(id) {
    companion object : IntEntityClass<Texture>(Textures)
    var type by Textures.type
    var url by Textures.url
    var player by Player referencedOn Textures.player
}

enum class TextureType {
    SKIN,
    CAPE,
    ;
}

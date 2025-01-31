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

import com.lgou2w.ldk.common.Enums
import com.lgou2w.ldk.common.notNull
import com.lgou2w.yggdrasil.YggdrasilLog
import com.lgou2w.yggdrasil.email.Email
import com.lgou2w.yggdrasil.email.EmailManager
import com.lgou2w.yggdrasil.security.HashedPassword
import com.lgou2w.yggdrasil.security.PasswordEncryption
import com.lgou2w.yggdrasil.util.UUIDSerializer
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.IntegerColumnType
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.TextColumnType
import org.jetbrains.exposed.sql.VarCharColumnType
import java.util.Collections
import java.util.UUID
import kotlin.reflect.KClass
import kotlin.system.exitProcess

object Dao {

    const val M_INITIALIZE = "初始化已注册 DAO 模型..."
    const val M_INITIALIZE_ERROR = "初始化 DAO 模型时错误:"

    val registers : Map<KClass<out Entity<*>>, Table> = Collections.unmodifiableMap(mapOf(
            User::class to Users,
            Player::class to Players,
            Texture::class to Textures,
            Token::class to Tokens
    ))

    fun initializeRegisters() {
        YggdrasilLog.info(M_INITIALIZE)
        try {
            SchemaUtils.create(*registers.values.toTypedArray())
        } catch (e: Exception) {
            YggdrasilLog.error(M_INITIALIZE_ERROR, e)
            exitProcess(1)
        }
    }

    open class UnsignedUUIDTable(name: String = "", columnName: String = "id") : IdTable<UUID>(name) {
        override val id: Column<EntityID<UUID>> = registerColumn<UUID>(columnName, UnsignedUUIDColumnType()).primaryKey()
            .clientDefault { UUID.randomUUID() }
            .entityId()
    }

    class UnsignedUUIDColumnType : ColumnType() {
        override fun sqlType(): String = "VARCHAR(32)"
        override fun notNullValueToDB(value: Any): Any
                = UUIDSerializer.fromUUID(valueToUUID(value)).notNull()
        private fun valueToUUID(value: Any): UUID {
            return when (value) {
                is UUID -> value
                is String -> UUIDSerializer.fromString(value).notNull()
                else -> error("Unexpected value of type UUID: ${value.javaClass.canonicalName}")
            }
        }
        override fun nonNullValueToString(value: Any): String
                = UUIDSerializer.fromUUID(valueToUUID(value)).notNull()
        override fun valueFromDB(value: Any): UUID
                = valueToUUID(value)
    }

    class EmailColumnType(
            private val emailManager: EmailManager
    ) : VarCharColumnType() {
        override fun notNullValueToDB(value: Any): Any {
            return valueToEmail(value).value
        }
        private fun valueToEmail(value: Any): Email {
            return when (value) {
                is Email -> value
                is String -> emailManager.parseEmail(value)
                else -> throw IllegalArgumentException("Value type is not Email.")
            }
        }
        override fun nonNullValueToString(value: Any): String {
            val email = valueToEmail(value)
            return super.nonNullValueToString(email.value)
        }
        override fun valueFromDB(value: Any): Any {
            return valueToEmail(value)
        }
    }

    class HashedPasswordColumnType(
            private val passwordEncryption: PasswordEncryption
    ) : TextColumnType() {
        override fun notNullValueToDB(value: Any): Any {
            return valueToHashed(value).hash
        }
        private fun valueToHashed(value: Any): HashedPassword {
            return when (value) {
                is HashedPassword -> value
                is String -> passwordEncryption.parse(value)
                else -> throw IllegalArgumentException("Value type is not HashedPassword.")
            }
        }
        override fun nonNullValueToString(value: Any): String {
            val hashed = valueToHashed(value)
            return super.nonNullValueToString(hashed.hash)
        }
        override fun valueFromDB(value: Any): Any {
            return valueToHashed(value)
        }
    }

    class PermissionColumnType : ColumnType() {
        override fun sqlType(): String = IntegerColumnType().sqlType()
        override fun notNullValueToDB(value: Any): Any {
            return when (value) {
                is Permission -> value.value
                is Int -> value
                else -> throw IllegalArgumentException("Value type is not Permission.")
            }
        }
        override fun valueFromDB(value: Any): Any {
            return when (value) {
                is Permission -> value
                is Int -> Enums.ofValuableNotNull(Permission::class.java, value)
                else -> throw IllegalArgumentException("Value type is not Permission.")
            }
        }
    }
}

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

package com.lgou2w.mcclake.yggdrasil.storage

import com.lgou2w.ldk.common.Runnable
import com.lgou2w.ldk.common.notNull
import com.lgou2w.mcclake.yggdrasil.YggdrasilConf
import com.lgou2w.mcclake.yggdrasil.YggdrasilLog
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.exposed.sql.Transaction
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

class StorageCoroutineContext(
        val transaction: Transaction,
        coroutine: CoroutineScope
) : CoroutineScope by coroutine {
    fun commit() = transaction.commit()
    fun rollback() = transaction.rollback()
}

abstract class Storage {

    abstract val type: String

    open fun initialize(conf: YggdrasilConf) {
        YggdrasilLog.info(Storages.M_INITIALIZE)
    }

    open fun shutdown() {
        YggdrasilLog.info(Storages.M_CLOSE)
    }

    abstract fun initializeDao(block: Runnable)

    abstract suspend fun <T> transaction(block: StorageCoroutineContext.() -> T): T

    override fun toString(): String {
        return "Storage(type=$type)"
    }
}

object Storages {

    const val M_INITIALIZE = "初始化数据存储器..."
    const val M_CLOSE = "关闭数据存储器..."

    const val STORAGE_SQLITE = "SQLite"
    const val STORAGE_MYSQL = "MySQL"
    const val UNSUPPORTED_EXCEPTION = "Unsupported data storage type: "

    val supportedNames: List<String> = Collections.unmodifiableList(arrayListOf(
            STORAGE_SQLITE,
            STORAGE_MYSQL
    )).notNull()

    val supportedMapping: Map<String, KClass<out Storage>> = Collections.unmodifiableMap(mapOf(
            STORAGE_SQLITE to StorageSQLite::class,
            STORAGE_MYSQL to StorageMySQL::class
    )).notNull()

    @Throws(UnsupportedOperationException::class)
    fun newStorage(type: String): Storage {
        val lookup = supportedMapping[type]
                     ?: throw UnsupportedOperationException(UNSUPPORTED_EXCEPTION + type)
        return lookup.createInstance()
    }
}

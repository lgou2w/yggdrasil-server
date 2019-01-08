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
import com.lgou2w.ldk.coroutines.CoroutineFactory
import com.lgou2w.ldk.coroutines.SimpleCoroutineFactory
import com.lgou2w.ldk.coroutines.SingleThreadDispatcherProvider
import com.lgou2w.ldk.sql.SQLiteConnectionFactory
import com.lgou2w.mcclake.yggdrasil.YggdrasilConf
import com.lgou2w.mcclake.yggdrasil.YggdrasilLog
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.nio.file.Paths
import java.sql.Connection

class StorageSQLite : Storage() {

    private var dProvider : SingleThreadDispatcherProvider? = null
    private var coroutineFactory : CoroutineFactory? = null
    private var cf : SQLiteConnectionFactory? = null

    override val type = "SQLite"

    override fun initialize(conf: YggdrasilConf) {
        super.initialize(conf)
        val database = conf.config.getString("${YggdrasilConf.ROOT}.storage.sqlite.database")
        YggdrasilLog.info("Use the SQLite database file : $database")
        dProvider = SingleThreadDispatcherProvider("storage")
        cf = SQLiteConnectionFactory(Paths.get(database))
        cf?.initialize()
        coroutineFactory = SimpleCoroutineFactory(dProvider.notNull())
        coroutineFactory?.launch {
            Database.connect({ cf.notNull().openSession() })
            TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
        }
    }

    override fun shutdown() {
        super.shutdown()
        cf?.shutdown()
        coroutineFactory = null
        dProvider?.dispatcher?.close()
        dProvider = null
        cf = null
    }

    override fun initializeDao(block: Runnable) {
        coroutineFactory.notNull().launch {
            transaction {
                block()
            }
        }
    }

    override suspend fun <T> transaction(block: CoroutineScope.() -> T): T {
        return coroutineFactory.notNull().with {
            org.jetbrains.exposed.sql.transactions.transaction {
                block()
            }
        }
    }
}

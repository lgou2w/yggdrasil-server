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

package com.lgou2w.yggdrasil.storage

import com.lgou2w.ldk.common.Runnable
import com.lgou2w.ldk.common.notNull
import com.lgou2w.ldk.coroutines.CoroutineFactory
import com.lgou2w.ldk.coroutines.FixedThreadPoolDispatcherProvider
import com.lgou2w.ldk.coroutines.SimpleCoroutineFactory
import com.lgou2w.ldk.sql.MySQLConnectionFactory
import com.lgou2w.ldk.sql.buildConfiguration
import com.lgou2w.yggdrasil.YggdrasilConf
import com.lgou2w.yggdrasil.YggdrasilLog
import org.jetbrains.exposed.sql.Database
import java.util.*

class StorageMySQL : Storage() {

    companion object {
        const val M_USE_THREADS = "使用协同程序中的最大线程数 : {}"

        val knownConfigurationKeys: List<String> = Collections.unmodifiableList(arrayListOf(
                "threads", "poolName", "address", "username", "password", "maxPoolSize",
                "minIdleConnections", "maxLifetime", "connectionTimeout"
        )).notNull()
    }

    private var dProvider : FixedThreadPoolDispatcherProvider? = null
    private var coroutineFactory : CoroutineFactory? = null
    private var cf : MySQLConnectionFactory? = null

    override val type = "MySQL"

    override fun initialize(conf: YggdrasilConf) {
        val threads = conf.config.getInt("${YggdrasilConf.ROOT}.storage.mysql.threads")
        YggdrasilLog.info(M_USE_THREADS, threads)
        val configuration = buildConfiguration {
            poolName = conf.getStringOrNull("${YggdrasilConf.ROOT}.storage.mysql.poolName")
            address = conf.config.getString("${YggdrasilConf.ROOT}.storage.mysql.address")
            database = conf.config.getString("${YggdrasilConf.ROOT}.storage.mysql.database")
            username = conf.config.getString("${YggdrasilConf.ROOT}.storage.mysql.username")
            password = conf.config.getString("${YggdrasilConf.ROOT}.storage.mysql.password")
            maxPoolSize = conf.config.getInt("${YggdrasilConf.ROOT}.storage.mysql.maxPoolSize")
            minIdleConnections = conf.config.getInt("${YggdrasilConf.ROOT}.storage.mysql.minIdleConnections")
            maxLifetime = conf.config.getLong("${YggdrasilConf.ROOT}.storage.mysql.maxLifetime")
            connectionTimeout = conf.config.getLong("${YggdrasilConf.ROOT}.storage.mysql.connectionTimeout")
            conf.config.entrySet().filter { !knownConfigurationKeys.contains(it.key) }.forEach {
                YggdrasilLog.info("= Property : ${it.key} = ${it.value.unwrapped()}")
                property = it.key to it.value.unwrapped()
            }
        }
        dProvider = FixedThreadPoolDispatcherProvider(threads, "storage")
        cf = MySQLConnectionFactory(configuration)
        cf?.initialize()
        coroutineFactory = SimpleCoroutineFactory(dProvider.notNull())
    }

    override fun shutdown() {
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

    override suspend fun <T> transaction(block: StorageCoroutineContext.() -> T): T {
        return coroutineFactory.notNull().with {
            Database.connect(cf.notNull().dataSource)
            org.jetbrains.exposed.sql.transactions.transaction {
                block(StorageCoroutineContext(this, this@with))
            }
        }
    }
}

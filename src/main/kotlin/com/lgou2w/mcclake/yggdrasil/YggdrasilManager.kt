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

package com.lgou2w.mcclake.yggdrasil

import com.lgou2w.ldk.common.notNull
import com.lgou2w.mcclake.yggdrasil.dao.Dao
import com.lgou2w.mcclake.yggdrasil.security.PasswordEncryption
import com.lgou2w.mcclake.yggdrasil.security.Passwords
import com.lgou2w.mcclake.yggdrasil.storage.Storage
import com.lgou2w.mcclake.yggdrasil.storage.Storages
import kotlinx.coroutines.CoroutineScope
import java.io.Closeable
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

interface YggdrasilService {

    val conf : YggdrasilConf

    val workDir : File

    val manager: YggdrasilManager

    val passwordEncryption: PasswordEncryption

    suspend fun <T> transaction(block: CoroutineScope.() -> T): T
}

class YggdrasilManager(val conf: YggdrasilConf) : Closeable {

    private val initialized = AtomicBoolean(false)
    private var mPasswordEncryption : PasswordEncryption? = null
    private var mStorage : Storage? = null

    fun initialize() {
        if (!initialized.compareAndSet(false, true))
            return
        YggdrasilLog.info("Initialize yggdrasil manager...")
        val storageType = conf.storageType
        val passwordEncryption = conf.passwordEncryption
        mPasswordEncryption = Passwords.newEncryption(passwordEncryption)
        mStorage = Storages.newStorage(storageType)
        mStorage?.initialize(conf)
        mStorage?.initializeDao { Dao.initializeRegisters() }
        YggdrasilLog.info("Use data storage type : $storageType")
        YggdrasilLog.info("Use password encryption type : $passwordEncryption")
        checkAndLogUnsafePasswordEncryption()
    }

    override fun close() {
        if (!initialized.compareAndSet(true, false))
            return
        YggdrasilLog.info("Close yggdrasil manager...")
        mPasswordEncryption = null
        mStorage?.shutdown()
        mStorage = null
    }

    private fun <T> T?.notNullField(): T = notNull("Manager has not been initialized.")

    val storage : Storage get() = mStorage.notNullField()
    val passwordEncryption : PasswordEncryption get() = mPasswordEncryption.notNullField()

    private fun checkAndLogUnsafePasswordEncryption() {
        val clazz = mPasswordEncryption?.javaClass
        if (clazz?.getAnnotation(Deprecated::class.java) != null) {
            YggdrasilLog.warn("Warning: The type of password encryption used is not secure.")
            YggdrasilLog.warn("It is recommended to use high-intensity password encryption type to ensure security.")
        }
    }
}
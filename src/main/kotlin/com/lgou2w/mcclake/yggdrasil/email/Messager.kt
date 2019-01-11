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

package com.lgou2w.mcclake.yggdrasil.email

import com.lgou2w.ldk.common.notNull
import com.lgou2w.ldk.coroutines.SimpleCoroutineFactory
import com.lgou2w.ldk.coroutines.SingleThreadDispatcherProvider
import com.lgou2w.mcclake.yggdrasil.YggdrasilConf
import com.lgou2w.mcclake.yggdrasil.YggdrasilLog
import java.io.Closeable

abstract class Messager : Closeable {

    interface Response

    companion object {
        private const val M_NAME = "messager"
    }

    private var dProvider : SingleThreadDispatcherProvider? = null
    protected lateinit var cf : SimpleCoroutineFactory
    protected lateinit var from : String

    open fun initialize(conf: YggdrasilConf) {
        YggdrasilLog.info("初始化邮件信徒中...")
        from = conf.messagerFrom
        dProvider = SingleThreadDispatcherProvider(M_NAME)
        cf = SimpleCoroutineFactory(dProvider.notNull())
    }

    @Throws(Exception::class)
    abstract suspend fun dispatch(
            to: List<String>,
            subject: String,
            content: String,
            isHtml: Boolean = false
    ): Messager.Response

    override fun close() {
        dProvider?.dispatcher?.close()
        dProvider = null
    }
}

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

import com.lgou2w.ldk.common.Version
import com.lgou2w.mcclake.yggdrasil.security.PasswordEncryption
import com.lgou2w.mcclake.yggdrasil.storage.StorageCoroutineContext
import io.ktor.server.engine.ApplicationEngineEnvironmentBuilder
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

object Yggdrasil {
    const val NAME = "Yggdrasil"
    val VERSION = Version(0, 1, 2)
}
object YggdrasilLog : Logger by LoggerFactory.getLogger(Yggdrasil.NAME)

fun main(args: Array<String>) {
    val conf = YggdrasilConf.load()
    val env = applicationEngineEnvironment { yggdrasilEnv(conf) }
    val server = embeddedServer(Netty, env) { yggdrasilNetty(conf) }
    server.start(false)
    server.waitStop("stop")
}

object DefaultYggdrasilService : YggdrasilService {
    override val conf: YggdrasilConf get() = managerImpl.conf
    override val workDir: File = File(System.getProperty("user.dir"))
    override val manager: YggdrasilManager get() = managerImpl
    override val passwordEncryption: PasswordEncryption get() = managerImpl.passwordEncryption
    override suspend fun <T> transaction(block: StorageCoroutineContext.() -> T): T
            = managerImpl.storage.transaction(block)
}

private lateinit var managerImpl: YggdrasilManager

private fun ApplicationEngineEnvironmentBuilder.yggdrasilEnv(conf: YggdrasilConf) {
    log = YggdrasilLog
    try {
        managerImpl = YggdrasilManager(conf)
        managerImpl.initialize()
    } catch (e: Exception) {
        YggdrasilLog.error("初始化 Yggdrasil 管理器时错误:", e)
        exitProcess(1)
    }
    module { yggdrasilApp(managerImpl) }
    conf.connectors.forEach { entry ->
        YggdrasilLog.info("配置连接器 : 主机 = {}, 端口 = {}", entry.first, entry.second)
        connector {
            host = entry.first
            port = entry.second
        }
    }
}

private fun NettyApplicationEngine.Configuration.yggdrasilNetty(conf: YggdrasilConf) {
    YggdrasilLog.info("配置 Netty 环境 : ")
    YggdrasilLog.info("= 请求队列限制 : ${conf.requestQueueLimit}")
    YggdrasilLog.info("= 并发运行限制 : ${conf.runningLimit}")
    YggdrasilLog.info("= 是否分享工作组 : ${conf.shareWorkGroup}")
    YggdrasilLog.info("= 响应写入超时秒数 : ${conf.responseWriteTimeoutSeconds}")
    requestQueueLimit = conf.requestQueueLimit
    runningLimit = conf.runningLimit
    shareWorkGroup = conf.shareWorkGroup
    responseWriteTimeoutSeconds = conf.responseWriteTimeoutSeconds
}

private fun NettyApplicationEngine.waitStop(input: String) {
    YggdrasilLog.info("键入 \"$input\" 以关闭应用程序...")
    runBlocking {
        val scanner = Scanner(System.`in`)
        while (scanner.next().equals(input, true))
            break
    }
    YggdrasilLog.info("关闭中...")
    try {
        managerImpl.close()
    } catch (e: Exception) {
        YggdrasilLog.error("关闭管理器时错误:", e)
    } finally {
        stop(1L, 5L, TimeUnit.SECONDS)
    }
}

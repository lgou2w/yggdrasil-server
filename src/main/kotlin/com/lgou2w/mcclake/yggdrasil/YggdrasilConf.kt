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
import com.lgou2w.ldk.common.isOrLater
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.nio.file.Files
import java.nio.file.Paths
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException
import kotlin.system.exitProcess

class YggdrasilConf private constructor(val config: Config, val version: Version) {

    val requestQueueLimit = config.getInt("$ROOT.netty.requestQueueLimit")
    val runningLimit = config.getInt("$ROOT.netty.runningLimit")
    val shareWorkGroup = config.getBoolean("$ROOT.netty.shareWorkGroup")
    val responseWriteTimeoutSeconds = config.getInt("$ROOT.netty.responseWriteTimeoutSeconds")

    val storageType : String = config.getString("$ROOT.storage.type")
    val passwordEncryption : String = config.getString("$ROOT.storage.passwordEncryption")

    val connectors : List<Pair<String, Int>> = config.getConfigList("$ROOT.connectors").map { connector ->
        val host = connector.getString("host")
        val port = connector.getInt("port")
        host to port
    }

    val userRegistrationEnable : Boolean = config.getBoolean("$ROOT.user.registration.enable")
    val userRegistrationPasswordVerify : Pattern = getPattern("$ROOT.user.registration.passwordVerify")
    val userRegistrationPasswordStrengthVerify : Pattern = getPattern("$ROOT.user.registration.passwordStrengthVerify")
    val userRegistrationNicknameVerify : Pattern = getPattern("$ROOT.user.registration.nicknameVerify")

    val userTokenValid : Long = config.getLong("$ROOT.user.token.valid")
    val userTokenValidMillis = userTokenValid * 1000L
    val userTokenInvalid : Long = config.getLong("$ROOT.user.token.invalid")
    val userTokenInvalidMillis = userTokenInvalid * 1000L

    fun getStringOrNull(path: String): String?
            = if (config.hasPath(path)) config.getString(path) else null

    private fun getPattern(path: String): Pattern = try {
        Pattern.compile(config.getString(path))
    } catch (e: PatternSyntaxException) {
        YggdrasilLog.error("路径中的正则表达式语法格式: $path", e)
        exitProcess(1)
    }

    companion object {

        const val ROOT = "yggdrasil"
        private const val NAME = "yggdrasil.conf"
        private const val NAME_OLD = "yggdrasil.conf.old"

        private fun Config.getVersion(path: String): Version {
            val versionOnly = getString(path)
            val regex = Pattern.compile("^(\\d+)\\.(\\d+)\\.(\\d+)$")
            val matcher = regex.matcher(versionOnly)
            var major = -1
            var minor = -1
            var build = -1
            if (matcher.matches()) {
                major = matcher.group(1).toInt()
                minor = matcher.group(2).toInt()
                build = matcher.group(3).toInt()
            }
            return Version(major, minor, build)
        }

        @JvmStatic
        private fun writConfiguration(classLoader: ClassLoader, configFile: File) {
            val input = InputStreamReader(classLoader.getResourceAsStream(NAME), Charsets.UTF_8)
            val output = OutputStreamWriter(FileOutputStream(configFile), Charsets.UTF_8)
            output.write(input.readText())
            output.flush()
            output.close()
            input.close()
        }

        @JvmStatic
        fun load(): YggdrasilConf {
            try {
                val classLoader = YggdrasilConf::class.java.classLoader
                val configFile = File(System.getProperty("user.dir"), NAME)
                val configCurrent = configFile.exists().let { if (it) ConfigFactory.parseFile(configFile) else null }
                val resourceReader = InputStreamReader(classLoader.getResourceAsStream(NAME), Charsets.UTF_8)
                val resourceConfig = ConfigFactory.parseReader(resourceReader)
                val currentVersion = configCurrent?.getVersion("$ROOT.version")
                val resourceVersion = resourceConfig.getVersion("$ROOT.version")
                if (!configFile.exists()) {
                    YggdrasilLog.info("检测到配置文件尚不存在...")
                    YggdrasilLog.info("系统退出, 请完成配置并再次启动应用程序...")
                    writConfiguration(classLoader, configFile)
                    exitProcess(0)
                }
                if (currentVersion != null && !currentVersion.isOrLater(resourceVersion)) {
                    YggdrasilLog.info("检测到的旧配置版本需要更新...")
                    YggdrasilLog.info("将旧配置文件复制到 $NAME_OLD")
                    Files.copy(configFile.toPath(), Paths.get(configFile.parent, NAME_OLD))
                    writConfiguration(classLoader, configFile)
                }
                resourceReader.close()
                YggdrasilLog.info("配置文件版本 : ${resourceVersion.version}")
                return YggdrasilConf(resourceConfig, resourceVersion)
            } catch (e: Exception) {
                YggdrasilLog.error("加载配置文件时错误:", e)
                exitProcess(1)
            }
        }
    }
}

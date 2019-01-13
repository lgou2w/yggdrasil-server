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

package com.lgou2w.yggdrasil.email

import com.lgou2w.ldk.common.isFalse
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import java.io.*

data class Template(val subject: String, val content: String)

object Templates {

    // SEE => resources/template/*.conf

    const val M_NOT_FOUND = "错误, 资源目录未找到指定文件: "

    const val T_REGISTER = "yggdrasil-template-register.conf"

    @Throws(IOException::class)
    private fun load(workDir: File, name: String, writable: Boolean): Config {
        val file = File(workDir, name)
        return if (!file.exists()) {
            // 从资源加载并写出到外部
            if (file.parentFile?.exists().isFalse())
                file.parentFile?.mkdirs()
            val stream = Template::class.java.classLoader.getResourceAsStream(name)
                         ?: throw FileNotFoundException(M_NOT_FOUND + name)
            val input = InputStreamReader(stream, Charsets.UTF_8)
            val config = ConfigFactory.parseReader(input)
            if (writable) {
                val input2 = InputStreamReader(Template::class.java.classLoader.getResourceAsStream(name), Charsets.UTF_8)
                val output = OutputStreamWriter(FileOutputStream(file), Charsets.UTF_8)
                output.write(input2.readText())
                output.flush()
                output.close()
                input2.close()
            }
            input.close()
            config
        } else {
            ConfigFactory.parseFile(file)
        }
    }

    private fun replaceArguments(source: String, vararg arguments: Pair<String, Any?>): String {
        var result = source
        arguments.forEach { result = result.replace(it.first, it.second.toString()) }
        return result
    }

    @Throws(IOException::class)
    fun parse(name: String, vararg arguments: Pair<String, Any?>): Template
            = parse(File(System.getProperty("user.dir")), name, false, *arguments)

    @Throws(IOException::class)
    fun parse(workDir: File, name: String, writable: Boolean, vararg arguments: Pair<String, Any?>): Template {
        val config = load(workDir, name, writable)
        val subject = config.getString("subject")
        val content = config.getString("content")
        return Template(
                replaceArguments(subject, *arguments),
                replaceArguments(content, *arguments)
        )
    }
}

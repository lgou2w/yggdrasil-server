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

package com.lgou2w.yggdrasil.manager

import com.lgou2w.ldk.rsa.RSAUtils
import com.lgou2w.yggdrasil.YggdrasilConf
import com.lgou2w.yggdrasil.YggdrasilLog
import com.lgou2w.yggdrasil.dao.Texture
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature
import java.util.*
import kotlin.system.exitProcess

class TexturesManager(private val conf: YggdrasilConf) {

    companion object {
        const val LENGTH = 64
        const val DIR = "textures"
        const val SIGNATURE = "SHA1WithRSA"
    }

    private val dir = File(conf.workDir, DIR)
    private lateinit var privateKey : PrivateKey
    private lateinit var publicKey : PublicKey
    private lateinit var publicKeyStr : String

    init {
        if (!dir.exists())
            dir.mkdirs()
        try {
            val file = File(dir, ".lock")
            if (!file.exists())
                file.createNewFile()
            RandomAccessFile(file, "rw").channel.tryLock()
        } catch (e: Exception) {
            YggdrasilLog.error("Textures lock failed:", e)
        }
        initializeKey()
    }

    private fun initializeKey() {
        try {
            var privateKeyFile = File(conf.userTexturesPrivateKey)
            var publicKeyFile = File(conf.userTexturesPublicKey)
            if (!privateKeyFile.isAbsolute)
                privateKeyFile = File(conf.workDir, conf.userTexturesPrivateKey)
            if (!publicKeyFile.isAbsolute)
                publicKeyFile = File(conf.workDir, conf.userTexturesPublicKey)

            YggdrasilLog.info("加载材质私钥文件 : ${privateKeyFile.absolutePath}")
            YggdrasilLog.info("加载材质公钥文件 : ${publicKeyFile.absolutePath}")

            privateKey = RSAUtils.decodePrivateKey(privateKeyFile.readText(Charsets.UTF_8))
            publicKeyStr = publicKeyFile.readText(Charsets.UTF_8)
            publicKey = RSAUtils.decodePublicKey(publicKeyStr)

            YggdrasilLog.info("加载成功, 材质公私钥已可用")
        } catch (e: Exception) {
            YggdrasilLog.error("加载材质公私钥时异常:", e)
            exitProcess(1)
        }
    }

    @Throws(IOException::class)
    fun signature(textureValue: String): String = try {
        val signature = Signature.getInstance(SIGNATURE)
        signature.initSign(privateKey)
        signature.update(textureValue.toByteArray(Charsets.UTF_8))
        signature.sign().let { Base64.getEncoder().encodeToString(it) }
    } catch (e: Exception) {
        throw IOException("无法进行材质签名数据.", e)
    }

    fun file(hash: String): File {
        return File(dir, hash)
    }

    // 包装材质的 URL 链接
    // 如果材质 URL 是远程链接，那么直接返回
    // 否则材质是相对，那么返回材质请求的链接点 + 材质路径
    fun wrapUrl(texture: Texture, scheme: String, host: String, port: Int): String {
        val url = texture.url
        if (url.startsWith("http")) // 外部材质链接，直接返回
            return url
        return buildString { // 本地材质链接，包装
            append(scheme).append("://")
            append(host)
            if (port != 80 || port != 443) // 如果端口不为 80 或 443 端口那么追加
                append(':').append(port)
            append('/')
            append(DIR)
            append('/')
            append(url) // 追加材质 Hash
        }
        // 本地包装后：http|https://your.domain:port/textures/hash
    }

    // 用于外部客户端进行材质签名的 PEM 格式公钥
    val externalPublicKey : String get() = publicKeyStr

    // 获取当前材质目录内存在的材质数量
    val files : Int get() = dir.list { _, name -> name.length == LENGTH }.size

    // 获取当前材质目录剩余的空间大小
    val freeSpace : Long get() = dir.freeSpace
}

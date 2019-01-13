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

import com.lgou2w.yggdrasil.YggdrasilConf
import com.lgou2w.yggdrasil.YggdrasilLog
import java.io.File
import java.io.RandomAccessFile

class TexturesManager(private val conf: YggdrasilConf) {

    private val dir = File(conf.workDir, "textures")

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
    }

    fun file(hash: String): File {
        return File(dir, hash)
    }

    // 获取当前材质目录内存在的材质数量
    val files : Int get() = dir.list { _, name -> name.length == 64 }.size

    // 获取当前材质目录剩余的空间大小
    val freeSpace : Long get() = dir.freeSpace
}

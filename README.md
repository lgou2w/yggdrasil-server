# Yggdrasil Server
A ktor-based minecraft yggdrasil account server

# 前言

TODO

# 结构

```
src/...
├── controller                                控制器
│   ├── Controller.kt                         抽象控制器
│   ├── AuthController.kt                     登录控制器
├── dao                                       数据库访问对象和模型
│   ├── Dao.kt                                用于初始化数据库和声明表结构的 DAO
│   ├── Player.kt                             玩家 DAO 模型（对于游戏内的玩家档案
│   ├── Texture.kt                            玩家材质 DAO 模型（玩家模型的材质档案
│   ├── Token.kt                              用户令牌 DAO 模型（登录，验证，登出操作
│   ├── User.kt                               用户 DAO 模型（用户的邮箱、密码数据等
├── error                                     异常错误类
│   ├── CommonException.kt                    常用异常类（404 未找到、服务器内部错误
│   ├── ForbiddenOperationException.kt        禁止操作的 403 异常
├── feature                                   功能特性
│   ├── RateLimiter.kt                        速率限制器 (WIP)
├── router                                    路由器
│   ├── authserver                            登录服务器路由器（用户登录、验证、登出操作
│   ├── sessionserver                         会话服务器路由器（玩家加入服务器、服务器验证、玩家档案获取
│   ├── textures                              材质路由器（材质的获取、材质的上传、删除
│   ├── Index.kt                              根路由器（返回规范 json 数据
│   ├── Routers.kt                            路由器操作类（已注册路由器等等
├── security                                  安全性
│   ├── Email.kt                              安全性邮箱类（邮箱数据类，解析字符串
│   ├── Password.kt                           安全性密码加密
│   ├── PasswordSalted.kt                     安全性密码和随机盐加密
│   ├── PasswordUnsalted.kt                   安全性密码无盐加密
├── storage                                   存储器
│   ├── Storage.kt                            抽象存储器
│   ├── StorageMySQL.kt                       MySQL 数据库存储器
│   ├── StorageSQLite.kt                      SQLite 数据库存储器
├── util                                      工具类
│   ├── DateTimeSerializer.kt                 Exposed 库 DateTime 的 Gson 序列化器
│   ├── Hash.kt                               用于计算字符串、图片哈希值的工具类
│   ├── Hex.kt                                用于生成十六进制字符串和将二进制编码的工具类
│   ├── UUIDSerializer.kt                     用于 UUID 的 Gson 序列化器，包含字符串解析
├── Yggdrasil.kt                              应用程序主类
├── YggdrasilApp.kt                           Ktor 的应用程序类
├── YggdrasilConf.kt                          应用程序配置文件
├── YggdrasilManager.kt                       应用程序管理器
│

resources/...
├── logback.xml                               slf4j 日志配置文件
├── yggdrasil.conf                            应用程序配置文件
│
```

# 技术栈

* [`Ktor`](https://github.com/ktorio/ktor)
* [`Gson`](https://github.com/Google/Gson)
* [`Exposed`](https://github.com/JetBrains/Exposed)
* [`Coroutines`](https://github.com/Kotlin/kotlinx.coroutines)

# 部署

* JDK 8
* JRE 8
* MySQL (可选)

```sh
git clone https://github.com/lgou2w/yggdrasil-server.git
cd yggdrasil-server
mvn clean package
```

在 `target` 目录获取已构建好的 JAR 文件. 然后运行 (x.y.z) 为版本号

```sh
java -jar yggdrasil-server-x.y.z.jar
```

配置你的 `yggdrasil.conf` 文件, 按需求开启功能. 最后访问主页查看响应.

# 协议

```
Copyright (C) 2019 The lgou2w (lgou2w@hotmail.com)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

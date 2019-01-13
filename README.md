# Yggdrasil Server
A ktor-based minecraft yggdrasil account server

# 介绍

TODO

# 功能特性

* [服务器信息](/src/main/kotlin/com/lgou2w/yggdrasil/router/Index.kt)
* [登录服务器](/src/main/kotlin/com/lgou2w/yggdrasil/router/authserver)
    * [注册](/src/main/kotlin/com/lgou2w/yggdrasil/router/authserver/Register.kt)
    * [验证码](/src/main/kotlin/com/lgou2w/yggdrasil/router/authserver/Verify.kt)
    * [登录](/src/main/kotlin/com/lgou2w/yggdrasil/router/authserver/Authenticate.kt)
    * [刷新](/src/main/kotlin/com/lgou2w/yggdrasil/router/authserver/Refresh.kt)
    * [验证](/src/main/kotlin/com/lgou2w/yggdrasil/router/authserver/Validate.kt)
    * [撤销](/src/main/kotlin/com/lgou2w/yggdrasil/router/authserver/Invalidate.kt)
    * [登出](/src/main/kotlin/com/lgou2w/yggdrasil/router/authserver/Signout.kt)
* [会话服务器](/src/main/kotlin/com/lgou2w/yggdrasil/router/sessionserver)
    * [客户端加入](/src/main/kotlin/com/lgou2w/yggdrasil/router/sessionserver/Join.kt) [WIP]
    * [服务端加入](/src/main/kotlin/com/lgou2w/yggdrasil/router/sessionserver/HasJoined.kt) [WIP]
    * [档案查询](/src/main/kotlin/com/lgou2w/yggdrasil/router/sessionserver/Profile.kt) [WIP]
* [材质服务器](/src/main/kotlin/com/lgou2w/yggdrasil/router/textures)
    * [材质获取](/src/main/kotlin/com/lgou2w/yggdrasil/router/textures/Obtain.kt) [WIP]
    * [材质上传](/src/main/kotlin/com/lgou2w/yggdrasil/router/textures/Upload.kt) [WIP]
* 更多
    * TODO

# 目录结构

<details>
<summary>查看结构</summary>

```
src/...
├── controller                                控制器
│   ├── Controller.kt                           抽象控制器
│   ├── AuthController.kt                       登录控制器
├── dao                                       数据库访问对象和模型
│   ├── Dao.kt                                  用于初始化数据库和声明表结构的 DAO
│   ├── Player.kt                               玩家 DAO 模型（对于游戏内的玩家档案
│   ├── Texture.kt                              玩家材质 DAO 模型（玩家模型的材质档案
│   ├── Token.kt                                用户令牌 DAO 模型（登录，验证，登出操作
│   ├── User.kt                                 用户 DAO 模型（用户的邮箱、密码数据等
├── error                                     异常错误类
│   ├── CommonException.kt                      常用异常类（404 未找到、500 服务器内部错误
│   ├── ForbiddenOperationException.kt          禁止操作的 403 异常
├── feature                                   功能特性
│   ├── RateLimiter.kt                          速率限制器 (WIP)
├── router                                    路由器
│   ├── authserver                              登录服务器路由器（用户登录、验证、登出操作
│   │   ├── Authenticate.kt                       POST 用户登录
│   │   ├── Invalidate.kt                         POST 吊销令牌
│   │   ├── Refresh.kt                            POST 刷新令牌
│   │   ├── Register.kt                           POST 用户注册
│   │   ├── Verify.kt                             POST 用户验证码
│   │   ├── Signout.kt                            POST 用户登出
│   │   ├── Validate.kt                           POST 验证令牌
│   ├── sessionserver                           会话服务器路由器（玩家加入服务器、服务器验证、玩家档案获取
│   │   ├── HasJoined.kt                          GET 服务端验证客户端 (WIP)
│   │   ├── Join.kt                               POST 客户端验证服务端 (WIP)
│   │   ├── Profile.kt                            GET 查询玩家档案 (WIP)
│   ├── textures                                材质路由器（材质的获取、材质的上传、删除
│   │   ├── Obtain.kt                             GET 获取指定材质 (WIP)
│   │   ├── Upload.kt                             POST 上传指定材质 (WIP)
│   ├── Index.kt                                根路由器（返回规范 json 数据
│   ├── Routers.kt                              路由器操作类（已注册路由器等等
├── security                                  安全性
│   ├── Email.kt                                安全性邮箱类（邮箱数据类，解析字符串
│   ├── Password.kt                             安全性密码加密
│   ├── PasswordSalted.kt                       安全性密码和随机盐加密
│   ├── PasswordUnsalted.kt                     安全性密码无盐加密
├── storage                                   存储器
│   ├── Storage.kt                              抽象存储器
│   ├── StorageMySQL.kt                         MySQL 数据库存储器
│   ├── StorageSQLite.kt                        SQLite 数据库存储器
├── util                                      工具类
│   ├── DateTimeSerializer.kt                   Exposed 库 DateTime 的 Gson 序列化器
│   ├── Hash.kt                                 用于计算字符串、图片哈希值的工具类
│   ├── Hex.kt                                  用于生成十六进制字符串和将二进制编码的工具类
│   ├── UUIDSerializer.kt                       用于 UUID 的 Gson 序列化器，包含字符串解析
├── Yggdrasil.kt                              应用程序入口
├── YggdrasilApp.kt                           应用程序 Ktor
├── YggdrasilConf.kt                          应用程序配置文件
├── YggdrasilManager.kt                       应用程序管理器

resources/...
├── logback.xml                               slf4j 日志配置文件
├── yggdrasil.conf                            应用程序配置文件
```

</details>

# 技术栈

* [`ldk`](https://github.com/lgou2w/ldk)
* [`Ktor`](https://github.com/ktorio/ktor)
* [`Gson`](https://github.com/Google/Gson)
* [`Exposed`](https://github.com/JetBrains/Exposed)
* [`Coroutines`](https://github.com/Kotlin/kotlinx.coroutines)

# 如何部署

* JDK 8
* JRE 8
* MySQL (可选)

```sh
git clone https://github.com/lgou2w/yggdrasil-server.git
cd yggdrasil-server
mvn clean package
```

在 `target` 目录获取已构建好的 `JAR` => `yggdrasil-server-x.y.z.jar` 文件.

1. 将 `JAR` 文件移动到适宜的目录
2. 双击运行 `JAR` 文件进行首次初始化
3. 用文本编辑工具打开 `yggdrasil.conf` 进行程序配置
4. 最后双击运行 `yggdrasil.sh` 文件运行并启动服务端

访问 [`localhost:9055`](http://localhost:9055) 查看服务端是否输出正常

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

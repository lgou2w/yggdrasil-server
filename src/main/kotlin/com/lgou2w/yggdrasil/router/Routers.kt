/*
 * Copyright (C) 2019 The lgou2w <lgou2w@hotmail.com>
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

package com.lgou2w.yggdrasil.router

import com.lgou2w.yggdrasil.DefaultYggdrasilService
import com.lgou2w.yggdrasil.YggdrasilLog
import com.lgou2w.yggdrasil.YggdrasilService
import com.lgou2w.yggdrasil.router.authserver.Authenticate
import com.lgou2w.yggdrasil.router.authserver.Invalidate
import com.lgou2w.yggdrasil.router.authserver.Refresh
import com.lgou2w.yggdrasil.router.authserver.Register
import com.lgou2w.yggdrasil.router.authserver.Signout
import com.lgou2w.yggdrasil.router.authserver.Validate
import com.lgou2w.yggdrasil.router.authserver.Verify
import com.lgou2w.yggdrasil.router.sessionserver.HasJoined
import com.lgou2w.yggdrasil.router.sessionserver.Join
import com.lgou2w.yggdrasil.router.sessionserver.Profile
import com.lgou2w.yggdrasil.router.textures.Obtain
import com.lgou2w.yggdrasil.router.textures.Upload
import io.ktor.routing.Routing
import java.util.Collections

interface RegisteredRouter {
    val path : String
}

class RegisteredRouterNode(
        override val path: String,
        val handler: RouterHandler
) : RegisteredRouter

class RegisteredRouterTree(
        override val path: String,
        val nodes: List<RegisteredRouter>
) : RegisteredRouter, Iterable<RegisteredRouter> {
    override fun iterator(): Iterator<RegisteredRouter> = nodes.iterator()
    val nodePaths : List<String>
        get() {
            val paths = ArrayList<String>()
            computeNodePaths(this, paths)
            return paths
        }
    private fun computeNodePaths(tree: RegisteredRouterTree, paths: MutableList<String>) {
        tree.nodes.forEach { node ->
            when (node) {
                is RegisteredRouterNode -> paths.add(node.path)
                is RegisteredRouterTree -> computeNodePaths(node, paths)
            }
        }
    }
}

fun routerTree(path: String, vararg node: RegisteredRouter): RegisteredRouterTree
        = RegisteredRouterTree(path, Collections.unmodifiableList(node.toList()))
fun routerNode(handler: RouterHandler): RegisteredRouterNode
        = RegisteredRouterNode(handler.path, handler)

interface RouterHandler {
    val path: String
    val method: String
    val yggdrasilService: YggdrasilService get() = DefaultYggdrasilService
    fun install(routing: Routing)
}

object Routers {

    const val M_INSTALL = "安装路由处理器 : {} = {}"

    val yggdrasil : RegisteredRouterTree = routerTree("/",
            routerNode(Index),
            routerTree("/authserver",
                    routerNode(Verify),
                    routerNode(Register),
                    routerNode(Authenticate),
                    routerNode(Refresh),
                    routerNode(Validate),
                    routerNode(Invalidate),
                    routerNode(Signout)
            ),
            routerTree("/sessionserver",
                    routerNode(Join),
                    routerNode(HasJoined),
                    routerNode(Profile)
            ),
            routerTree("/textures",
                    routerNode(Obtain),
                    routerNode(Upload)
            )
    )

    val yggdrasilPaths by lazy { yggdrasil.nodePaths }

    fun install(routing: Routing) {
        install(routing, yggdrasil)
    }

    private fun install(routing: Routing, tree: RegisteredRouterTree) {
        tree.nodes.forEach { node ->
            when (node) {
                is RegisteredRouterNode -> {
                    YggdrasilLog.info(M_INSTALL, node.handler.method, node.path)
                    node.handler.install(routing)
                }
                is RegisteredRouterTree -> install(routing, node)
            }
        }
    }
}

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

package com.lgou2w.yggdrasil

import com.lgou2w.yggdrasil.error.ForbiddenOperationException
import com.lgou2w.yggdrasil.error.InternalServerException
import com.lgou2w.yggdrasil.error.NotFoundException
import com.lgou2w.yggdrasil.error.UnauthorizedException
import com.lgou2w.yggdrasil.router.Routers
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.features.StatusPages
import io.ktor.gson.GsonConverter
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.routing

fun Application.yggdrasilApp(manager: YggdrasilManager) {
    install(DefaultHeaders) {
        manager.conf.httpHeaders.forEach { header ->
            header(header.first, header.second)
        }
    }
    install(CallLogging)
    install(ContentNegotiation) {
        register(ContentType.Application.Json, GsonConverter(manager.gson))
    }
    install(StatusPages) {
        exception<ForbiddenOperationException> { error -> call.respond(HttpStatusCode.Forbidden, error.response()) }
        exception<UnauthorizedException> { call.respond(HttpStatusCode.Unauthorized) }
        exception<NotFoundException> { call.respond(HttpStatusCode.NotFound) }
        exception<NotImplementedError> { call.respond(HttpStatusCode.NotImplemented) }
        exception<InternalServerException> { call.respond(HttpStatusCode.InternalServerError) }
        exception<Throwable> { error -> call.respond(HttpStatusCode.InternalServerError); throw error }
    }
    routing {
        Routers.install(this)
    }
}

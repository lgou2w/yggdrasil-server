package com.lgou2w.mcclake.yggdrasil.router.authserver

import com.lgou2w.mcclake.yggdrasil.controller.AuthController
import com.lgou2w.mcclake.yggdrasil.router.RouterHandler
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.accept
import io.ktor.routing.post

/**
 * ## POST request when verifying a new registration.
 *
 * * RateLimiter: key = ip
 * * Request:
 * ```json
 * {
 *   "username": "user email",
 *   "nickname": "user nickname"
 * }
 * ```
 */
object Send : RouterHandler {

    override val method: String = "POST"
    override val path: String = "/authserver/send"

    override fun install(routing: Routing) {
        routing.accept(ContentType.Application.Json) {
            routing.post(path) {
                if (!yggdrasilService.conf.userRegistrationEnable) {
                    // Registration is not allowed
                    // Response 405 Method Not Allowed
                    call.respond(HttpStatusCode.MethodNotAllowed)
                } else {
                    val parameters = call.receive<Map<String, Any?>>()
                    val email = parameters["username"]?.toString()
                    val nickname = parameters["nickname"]?.toString()
                    val response = AuthController.send(email, nickname)
                    call.respond(response)
                }
            }
        }
    }
}

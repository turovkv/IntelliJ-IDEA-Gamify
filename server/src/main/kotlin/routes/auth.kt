package com.intellij.gamify.server.routes

import com.intellij.gamify.server.repository.GamifyRepository
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.routing.*
import io.ktor.util.*
import io.ktor.util.pipeline.*

private val AUTH_REPO = AttributeKey<GamifyRepository.Authorized>("ACCESS_REPO")

fun Application.installHashedAuthentication(repository: GamifyRepository) {
    install(Authentication) {
        basic("auth-basic-hashed") {
            validate { credentials ->
                val authorized = repository.authenticate(credentials)
                if (authorized != null) {
                    attributes.put(AUTH_REPO, authorized)
                }
                return@validate authorized?.userPrincipal
            }
        }
    }
}

fun Route.authenticateByHash(block: Route.() -> Unit) {
    authenticate("auth-basic-hashed") {
        println("HELLO :(")
        block(this)
    }
}

val PipelineContext<*, ApplicationCall>.repository: GamifyRepository.Authorized
    get() = this.context.attributes[AUTH_REPO]


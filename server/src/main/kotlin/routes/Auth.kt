package com.intellij.gamify.server.routes

import com.intellij.gamify.server.repository.GamifyRepository
import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.authenticate
import io.ktor.auth.basic
import io.ktor.routing.Route
import io.ktor.util.AttributeKey
import io.ktor.util.pipeline.PipelineContext

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
        block(this)
    }
}

val PipelineContext<*, ApplicationCall>.repository: GamifyRepository.Authorized
    get() = this.context.attributes[AUTH_REPO]


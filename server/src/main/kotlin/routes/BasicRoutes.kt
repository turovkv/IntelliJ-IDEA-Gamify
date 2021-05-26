package com.intellij.gamify.server.routes

import com.intellij.gamify.server.entities.UserInfo
import com.intellij.gamify.server.repository.GamifyRepository
import com.intellij.gamify.server.repository.RepositoryException
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

private fun Route.basicRouting(repository: GamifyRepository) {
    route("/users") {

        //getAllUserInfos
        get("") {
            call.respond(repository.getAllUserInfos())
        }

        //getUserInfoByName
        get("{name}") {
            try {
                val name = call.parameters["name"]!!
                val user = repository.getUserInfoByName(name)
                call.respond(user)
            } catch (e: RepositoryException) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    e.localizedMessage
                )
            }
        }

        //createUser
        post("") {
            val credential = call.receive<UserPasswordCredential>()

            try {
                repository.createUser(credential)
                call.respond(HttpStatusCode.OK)
            } catch (e: RepositoryException) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    e.localizedMessage
                )
            }
        }
    }
}

private fun Route.basicRoutingWithAuth() {
    route("/users") {
        //updateUser
        put("update") {
            try {
                val userInfo = call.receive<UserInfo>()
                repository.updateUser(userInfo)
                call.respond(HttpStatusCode.OK)
            } catch (e: RepositoryException) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    e.localizedMessage
                )
            }
        }
    }
}


fun Application.registerBasicRoutes(repository: GamifyRepository) {
    routing {
        basicRouting(repository)
        authenticateByHash {
            basicRoutingWithAuth()
        }
    }
}

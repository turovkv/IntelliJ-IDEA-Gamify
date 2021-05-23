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

fun Route.basicRouting(repository: GamifyRepository) {
    route("/users") {

        //getAllUserInfos
        get("") {
            call.respond(repository.getAllUserInfos())
        }

        //getUserInfoById
        get("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()

            if (id == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    "id parameter has to be a number"
                )
                return@get
            }

            try {
                val user = repository.getUserInfoById(id)
                call.respond(user)
            } catch (e: RepositoryException) {
                call.respond(
                    HttpStatusCode.NotFound,
                    e.localizedMessage
                )
            }
        }

        //addEmptyUser
        post("") {
            val credential = call.receive<UserPasswordCredential>()

            try {
                val id: Int = repository.createUser(credential)
                call.respond(id)
            } catch (e: RepositoryException) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    e.localizedMessage
                )
                return@post
            }
        }

    }
}

fun Route.basicRoutingWithAuth(repository: GamifyRepository) {
    route("/users") {

        //updateUser
        put("{id}") {
            val userId = call.parameters["id"]?.toIntOrNull()

            if (userId == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    "id parameter has to be a number!"
                )
                return@put
            }

            try {
                val userInfo = call.receive<UserInfo>()
                repository.checkAccess(userId, call.principal<UserIdPrincipal>()?.name)
                repository.updateUser(userId, userInfo)
                call.respond(HttpStatusCode.OK)
            } catch (e: RepositoryException) {
                call.respond(
                    HttpStatusCode.NotFound,
                    e.localizedMessage
                )
            }
        }

        //deleteUser
        delete("{id}") {
            val userId = call.parameters["id"]?.toIntOrNull()

            if (userId == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    "id parameter has to be a number!"
                )
                return@delete
            }


            try {
                repository.checkAccess(userId, call.principal<UserIdPrincipal>()?.name)
                repository.deleteUser(userId)
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
        authenticate("auth-basic-hashed") {
            basicRoutingWithAuth(repository)
        }
    }
}

package com.intellij.gamify.server.routes

import com.intellij.gamify.server.entities.Notification
import com.intellij.gamify.server.repository.GamifyRepository
import com.intellij.gamify.server.repository.RepositoryException
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

private fun Route.notificationRouting(repository: GamifyRepository) {
    route("/users") {
        get("notifications/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()

            if (id == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    "id parameter has to be a number"
                )
                return@get
            }

            try {
                repository.checkAccess(id, call.principal<UserIdPrincipal>()?.name)
                call.respond(repository.getNotifications(id))
            } catch (e: RepositoryException) {
                call.respond(
                    HttpStatusCode.NotFound,
                    e.localizedMessage
                )
            }
        }

        post("notifications/{id}") {
            val idFrom = call.parameters["id"]?.toIntOrNull()
            if (idFrom == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    "id parameter has to be a number!"
                )
                return@post
            }

            val notification = call.receive<Notification>()

            try {
                repository.checkAccess(idFrom, call.principal<UserIdPrincipal>()?.name)
                repository.addNotification(idFrom, notification)
            } catch (e: RepositoryException) {
                call.respond(
                    HttpStatusCode.NotFound,
                    e.localizedMessage
                )
            }
        }

//        get("subscribing/{id}") {
//            val id = call.parameters["id"]?.toIntOrNull()
//
//            if (id == null) {
//                call.respond(
//                    HttpStatusCode.BadRequest,
//                    "id parameter has to be a number"
//                )
//                return@get
//            }
//
//            try {
//                val user = repository.getUserById(id)
//                call.respond(user.subscribing.toList())
//            } catch (e: RepositoryException) {
//                call.respond(
//                    HttpStatusCode.NotFound,
//                    e.localizedMessage
//                )
//            }
//        }

        post("subscribing/{id}") {
            val idFrom = call.parameters["id"]?.toIntOrNull()
            if (idFrom == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    "id parameter has to be a number!"
                )
                return@post
            }

            val idTo = call.receive<Int>()
            try {
                repository.checkAccess(idFrom, call.principal<UserIdPrincipal>()?.name)
                repository.subscribe(idFrom, idTo)
                call.respond(HttpStatusCode.OK)
            } catch (e: RepositoryException) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    e.localizedMessage
                )
            }
        }

        delete("subscribing/{id}") {
            val idFrom = call.parameters["id"]?.toIntOrNull()
            if (idFrom == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    "id parameter has to be a number!"
                )
                return@delete
            }

            val idTo = call.receive<Int>()
            try {
                repository.checkAccess(idFrom, call.principal<UserIdPrincipal>()?.name)
                repository.unsubscribe(idFrom, idTo)
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

fun Application.registerNotificationRoutes(repository: GamifyRepository) {
    routing {
        authenticate("auth-basic-hashed") {
            notificationRouting(repository)
        }
    }
}

package com.intellij.gamify.server.routes

import com.intellij.gamify.server.entities.Notification
import com.intellij.gamify.server.repository.RepositoryException
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

private fun Route.notificationRouting() {
    route("/users") {
        get("notifications") {
            try {
                call.respond(repository.getNotifications())
            } catch (e: RepositoryException) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    e.localizedMessage
                )
            }
        }

        post("notifications") {
            try {
                val notification = call.receive<Notification>() // exception
                repository.addNotification(notification)
            } catch (e: RepositoryException) {
                call.respond(
                    HttpStatusCode.NotFound,
                    e.localizedMessage
                )
            }
        }

//        get("subscribing/{name}") {
//            val name = call.parameters["name"]?.toIntOrNull()
//
//            if (name == null) {
//                call.respond(
//                    HttpStatusCode.BadRequest,
//                    "name parameter has to be a number"
//                )
//                return@get
//            }
//
//            try {
//                val user = repository.getUserById(name)
//                call.respond(user.subscribing.toList())
//            } catch (e: RepositoryException) {
//                call.respond(
//                    HttpStatusCode.NotFound,
//                    e.localizedMessage
//                )
//            }
//        }

        post("subscribing/{nameTo}") {
            try {
                val nameTo = call.parameters["nameTo"]!!
                repository.subscribe(nameTo)
                call.respond(HttpStatusCode.OK)
            } catch (e: RepositoryException) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    e.localizedMessage
                )
            }
        }

        delete("subscribing/{nameTo}") {
            try {
                val nameTo = call.parameters["nameTo"]!!
                repository.unsubscribe(nameTo)
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

fun Application.registerNotificationRoutes() {
    routing {
        authenticateByHash {
            notificationRouting()
        }
    }
}

package com.intellij.gamify.server.routes

import com.intellij.gamify.server.entities.Notification
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.delete
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.routing.routing

private fun Route.notificationRouting() {
    route("/users/with-auth") {
        //get notifications
        get("notifications") {
            handleResponse {
                return@handleResponse call.respond(repository.getNotifications())
            }
        }

        //add my notification
        post("notifications") {
            handleResponse {
                val notification = call.receive<Notification>()
                repository.addNotification(notification)
                return@handleResponse HttpStatusCode.OK
            }
        }

        //subscribe
        post("subscribing/{nameTo}") {
            handleResponse {
                val nameTo = call.parameters["nameTo"]!!
                repository.subscribe(nameTo)
                return@handleResponse HttpStatusCode.OK
            }
        }

        //unsubscribe
        delete("subscribing/{nameFrom}") {
            handleResponse {
                val nameFrom = call.parameters["nameFrom"]!!
                repository.unsubscribe(nameFrom)
                return@handleResponse HttpStatusCode.OK
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

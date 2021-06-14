package com.intellij.gamify.server.repository

import com.intellij.gamify.server.entities.User
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

class MongoImplRepository {
    val client = KMongo.createClient().coroutine //use coroutine extension
    val database = client.getDatabase("test") //normal java driver usage
    val col = database.getCollection<User>()
}

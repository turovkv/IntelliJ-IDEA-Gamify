plugins {
    //kotlin("jvm") version "1.5.0"
    application
}

application {
    mainClass.set("com.intellij.gamify.server.Application.kt")
}

group = "com.intellij.gamify.server"
version = "1"

repositories {
    mavenCentral()
    jcenter()
}

val ktor_version: String by project
val logback_version: String by project

dependencies {
    implementation( "io.ktor:ktor-server-core:$ktor_version")
    implementation( "io.ktor:ktor-server-netty:$ktor_version")
    implementation( "ch.qos.logback:logback-classic:$logback_version")
    implementation("io.ktor:ktor-auth:$ktor_version")
    implementation("io.ktor:ktor-gson:$ktor_version")

    testImplementation( "io.ktor:ktor-server-tests:$ktor_version")
}

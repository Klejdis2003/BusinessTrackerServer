package com.klejdis.services

import com.klejdis.services.config.rebuildDatabase
import com.klejdis.services.plugins.*
import io.github.cdimascio.dotenv.Dotenv
import io.ktor.network.tls.certificates.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.koin.core.context.startKoin
import org.koin.java.KoinJavaComponent.inject
import java.io.File

val MODE = Mode.DEV

fun main() {
    startKoin{ modules(appModule) }
    embeddedServer(
        Netty,
        configure = {
            enableHttp2 = true
            configureSSL()
        },
        module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureHTTPSRedirect()
    configureSecurity()
    configureSerialization()
    configureRouting()
    configureSessions()
    rebuildDatabase()
}

fun ApplicationEngine.Configuration.configureSSL() {
    val keystoreFile = File("keystore.jks")
    val vault by inject<Dotenv>(clazz = Dotenv::class.java)
    val keystore = buildKeyStore {
        certificate("ssl") {
            password = vault["PRIVATE_KEY_PASSWORD"]
            domains = listOf("localhost")
        }
    }

    sslConnector(
        keyStore = keystore,
        keyAlias = "ssl",
        keyStorePassword = { vault["KEYSTORE_PASSWORD"].toCharArray()},
        privateKeyPassword = { vault["PRIVATE_KEY_PASSWORD"].toCharArray()}
    ) {
        port = 8080
        keyStorePath = keystoreFile
    }
}

enum class Mode {
    DEV, PROD
}

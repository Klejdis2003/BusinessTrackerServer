package com.klejdis.services

import com.klejdis.services.config.rebuildDatabase
import com.klejdis.services.plugins.*
import io.ktor.http.*
import io.ktor.network.tls.certificates.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.koin.core.context.startKoin
import java.io.File

val MODE = Mode.valueOf(System.getenv("MODE") ?: "DEV")
val URL_PROTOCOL = when(MODE) {
    Mode.DEV -> URLProtocol.HTTPS
    Mode.PROD -> URLProtocol.HTTP
}
val SSL_PORT = System.getenv("SSL_PORT")?.toInt() ?: 8443
val URL_PORT = System.getenv("PORT")?.toInt() ?: SSL_PORT


fun main() {
    startKoin { modules(appModule, businessServicesModule) }
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
    configureLogger()
    rebuildDatabase()
}

fun ApplicationEngine.Configuration.configureSSL() {
    val env = System.getenv()
    val keystoreFile = File("keystore.jks")
    val keystore = buildKeyStore {
        certificate("ssl") {
            password = env["PRIVATE_KEY_PASSWORD"]!!
            domains = listOf("localhost")
        }
    }

    sslConnector(
        keyStore = keystore,
        keyAlias = "ssl",
        keyStorePassword = { env["KEYSTORE_PASSWORD"]!!.toCharArray() },
        privateKeyPassword = { env["PRIVATE_KEY_PASSWORD"]!!.toCharArray() }
    ) {
        port = SSL_PORT
        keyStorePath = keystoreFile
    }
    connector {
        port = System.getenv("PORT")?.toInt() ?: 8080
    }


}

enum class Mode {
    DEV, PROD
}

package com.klejdis.services

import ch.qos.logback.classic.LoggerContext
import com.klejdis.services.config.rebuildDatabase
import com.klejdis.services.plugins.configureRouting
import com.klejdis.services.plugins.configureSecurity
import com.klejdis.services.plugins.configureSerialization
import com.klejdis.services.plugins.configureSessions
import io.ktor.http.*
import io.ktor.network.tls.certificates.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.util.logging.*
import org.koin.core.context.startKoin
import org.slf4j.LoggerFactory
import java.io.File

val MODE = Mode.valueOf(System.getenv("MODE") ?: "DEV")
val URL_PROTOCOL = when(MODE) {
    Mode.DEV -> URLProtocol.HTTPS
    Mode.PROD -> URLProtocol.HTTP
}
val URL_PORT = System.getenv("PORT")?.toInt() ?: 8443

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
    //configureHTTPSRedirect()
    configureSecurity()
    configureSerialization()
    configureRouting()
    configureSessions()
    rebuildDatabase()
    val l =  LoggerFactory.getILoggerFactory() as LoggerContext
    l.getLogger(Logger.ROOT_LOGGER_NAME).level = ch.qos.logback.classic.Level.INFO
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
        port = 8443
        keyStorePath = keystoreFile
    }
    connector {
        port = System.getenv("PORT")?.toInt() ?: 8080
    }


}

enum class Mode {
    DEV, PROD
}

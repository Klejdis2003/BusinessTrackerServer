package com.klejdis.services

import com.klejdis.services.config.rebuildDatabase
import com.klejdis.services.plugins.*
import com.klejdis.services.services.OAuthenticationService
import io.github.cdimascio.dotenv.Dotenv
import io.ktor.network.tls.certificates.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.dotenv.vault.DotenvVault
import org.dotenv.vault.dotenvVault
import org.koin.core.context.startKoin
import org.koin.java.KoinJavaComponent.inject
import org.koin.mp.KoinPlatform.getKoin
import java.io.File

val MODE = Mode.DEV

fun main() {
    startKoin {
        modules(appModule)
        getKoin().get<OAuthenticationService>() // initialize the service
    }
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
    //configureKoin()
    configureHTTPSRedirect()
    configureSecurity()
    configureSerialization()
    configureRouting()
    configureSessions()
    rebuildDatabase()

}

fun ApplicationEngine.Configuration.configureSSL() {
    val keystoreFile = File("keystore.jks")
    val vault = dotenvVault ()
    val keystore = buildKeyStore {
        certificate("ssl") {
            password = vault["PRIVATE_KEY_PASSWORD"]
            domains = listOf("localhost")
        }
    }

    sslConnector(
        keyStore = keystore,
        keyAlias = "ssl",
        keyStorePassword = { vault["KEYSTORE_PASSWORD"].toCharArray() },
        privateKeyPassword = { vault["PRIVATE_KEY_PASSWORD"].toCharArray() }
    ) {
        port = 8080
        keyStorePath = keystoreFile
    }
}

enum class Mode {
    DEV, PROD
}

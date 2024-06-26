package com.klejdis.services


import com.klejdis.services.config.JwtConfig
import com.klejdis.services.config.postgresDatabase
import com.klejdis.services.dto.BusinessMapper
import com.klejdis.services.dto.ItemMapper
import com.klejdis.services.dto.OrderMapper
import com.klejdis.services.repositories.*
import com.klejdis.services.services.*
import io.github.cdimascio.dotenv.Dotenv
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.dotenv.vault.dotenvVault
import org.koin.dsl.module

val appModule = module {
    //config
    single<Dotenv> { dotenvVault() }
    single<JwtConfig> { JwtConfig(get()) }
    single<HttpClient> { HttpClient(CIO){
        install(ContentNegotiation){
            json(Json {
                prettyPrint = true
                ignoreUnknownKeys = true
            })
        } 

    }
    }
    //repositories
    single<BusinessRepository> { BusinessRepositoryImpl(postgresDatabase) }
    single<ItemRepository> { ItemRepositoryImpl(postgresDatabase) }
    single<OrderRepository> { OrderRepositoryImpl(postgresDatabase) }

    //mappers
    single<BusinessMapper> { BusinessMapper() }
    single<OrderMapper> { OrderMapper(ItemMapper()) }
    single<ItemMapper> { ItemMapper() }

    //services

    single<BusinessService> { BusinessService(get(), get(), get(), get()) }
    single<OAuthenticationService> {
        OAuthenticationServiceImpl(get(), get(), get())
    }
    single<OrderService> { OrderService(get(), get()) }


}


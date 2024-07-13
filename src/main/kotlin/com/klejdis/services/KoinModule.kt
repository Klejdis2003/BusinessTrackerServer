package com.klejdis.services


import com.klejdis.services.config.postgresDatabase
import com.klejdis.services.dto.BusinessMapper
import com.klejdis.services.dto.CustomerMapper
import com.klejdis.services.dto.ItemMapper
import com.klejdis.services.dto.OrderMapper
import com.klejdis.services.model.Session
import com.klejdis.services.repositories.*
import com.klejdis.services.services.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import org.koin.mp.KoinPlatform.getKoin

val appModule = module {
    //config
    single<Json> {
        Json {
            prettyPrint = true
            ignoreUnknownKeys = true
        }
    }
    single<HttpClient> {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(get())
            }

        }
    }
    //repositories
    single<BusinessRepository> { BusinessRepositoryKtorm(postgresDatabase) }
    single<ItemRepository> { ItemRepositoryKtorm(postgresDatabase) }
    single<OrderRepository> { OrderRepositoryKtorm(postgresDatabase) }
    single<ExpenseRepository> { ExpenseRepositoryKtorm(postgresDatabase) }
    single<CustomerRepository> { CustomerRepositoryKtorm(postgresDatabase) }

    //mappers
    single<BusinessMapper> { BusinessMapper() }
    single<CustomerMapper> { CustomerMapper() }
    single<ItemMapper> { ItemMapper() }
    single<OrderMapper> { OrderMapper(get(), get()) }

    single<OAuthenticationService> {
        OAuthenticationServiceImpl(get())
    }


}

val businessServicesModule = module {
    scope<Session>{
        scoped<OrderService> { (loggedInEmail: String) ->
            OrderService(get(), get(), get(), get(), loggedInEmail)
        }
        scoped<BusinessService> {
            (loggedInEmail: String) ->
            BusinessService(get(), get(), get(), get(), get(), loggedInEmail)
        }

        scoped<ExpenseService> {
            (loggedInEmail: String) ->
            ExpenseService(get(), get(), loggedInEmail)
        }
        scoped<CustomerService> { (loggedInEmail: String) ->
            CustomerService(get(), loggedInEmail)
        }

        scoped<AnalyticsService> { (loggedInEmail: String) ->
            AnalyticsService(get(), get(), loggedInEmail)
        }

    }
}

fun startKoinBusinessScope(loggedInEmail: String) {
    val scope = getKoin().getOrCreateScope<Session>(loggedInEmail)

    scope.get<BusinessService> { parametersOf(loggedInEmail) }
    scope.get<OrderService> { parametersOf(loggedInEmail) }
    scope.get<ExpenseService> { parametersOf(loggedInEmail) }
    scope.get<CustomerService> { parametersOf(loggedInEmail) }
    scope.get<AnalyticsService> { parametersOf(loggedInEmail) }
}

fun endKoinBusinessScope(loggedInEmail: String) {
    val scope = getKoin().getScopeOrNull(loggedInEmail)
    scope?.let {
        println("Closing scope for $loggedInEmail")
        scope.close()
    }
}


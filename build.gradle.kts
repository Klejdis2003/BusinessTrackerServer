val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val ktorm_version: String by project
val postgresql_driver_version: String by project
val koin_version: String by project
val swagger_codegen_version: String by project

plugins {
    kotlin("jvm") version "2.0.0"
    id("io.ktor.plugin") version "3.0.0-beta-1"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.0"
    id("org.jetbrains.dokka") version "1.9.20"
}

group = "com.klejdis.services"
version = "0.0.1"

//add a task to run in prod mode
tasks.register("run_prod") {
    project.ext.set("development", false)
}

//add a task to run in dev mode
tasks.register("run_dev") {
    project.ext.set("development", true)
}

ktor {
    docker {
        localImageName = "klejdis-business-analytics-service"
        jreVersion.set(JavaVersion.VERSION_21)
        localImageName.set("klejdis-business-analytics-service")
        imageTag.set("klejdisanalytics.azurecr.io/klejdis-business-analytics-service:latest")
        jib{
            to {
                image = "klejdisanalytics.azurecr.io/klejdis-business-analytics-service:latest"
            }
        }
    }
}



tasks.dokkaHtml {
    outputDirectory.set(File("src/main/resources/documentation/code"))
}

tasks.dokkaGfm {
    outputDirectory.set(layout.buildDirectory.dir("documentation/markdown"))
}

application {
    mainClass.set("com.klejdis.services.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-auth-jvm")
    implementation("io.ktor:ktor-server-http-redirect")
    implementation("io.ktor:ktor-server-auth-jwt-jvm")
    implementation("io.ktor:ktor-network-tls-certificates")
    implementation("io.ktor:ktor-server-content-negotiation")
    implementation("io.ktor:ktor-client-content-negotiation")
    implementation("io.ktor:ktor-server-html-builder:$ktor_version")
    implementation("io.ktor:ktor-server-swagger:$ktor_version")
    implementation("io.ktor:ktor-server-openapi:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json")
    implementation("io.ktor:ktor-server-host-common")
    implementation("io.ktor:ktor-server-netty")
    implementation("io.ktor:ktor-client-core")
    implementation("io.ktor:ktor-client-cio")
    implementation("io.insert-koin:koin-ktor:$koin_version")
    implementation("org.postgresql:postgresql:$postgresql_driver_version")
    implementation("org.ktorm:ktorm-core:$ktorm_version")
    implementation("org.ktorm:ktorm-support-postgresql:$ktorm_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("com.github.dotenv-org:dotenv-vault-kotlin:0.0.3")
    implementation("io.ktor:ktor-client-cio-jvm:3.0.0-beta-1")
    testImplementation("io.ktor:ktor-server-tests-jvm")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
    testImplementation("io.ktor:ktor-server-test-host-jvm:3.0.0-beta-1")
}

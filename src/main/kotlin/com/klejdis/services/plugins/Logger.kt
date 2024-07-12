package com.klejdis.services.plugins

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import io.ktor.server.application.*
import org.slf4j.LoggerFactory

fun Application.configureLogger() {
    val l =  LoggerFactory.getILoggerFactory() as LoggerContext
    l.getLogger(Logger.ROOT_LOGGER_NAME).level = ch.qos.logback.classic.Level.INFO
}
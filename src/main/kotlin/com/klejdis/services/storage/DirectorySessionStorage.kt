package com.klejdis.services.storage

import com.klejdis.services.util.TimeFrame
import io.ktor.server.sessions.*
import java.util.concurrent.ConcurrentHashMap

/**
 * An in memory session id to token mapper, used for securely accessing tokens without exposing them
 * externally.
 */
class InMemoryLoginSessionStorage(
    private val sessionMaxAgeInSeconds: TimeFrame = TimeFrame.days(1)
) : SessionStorage{
    private val sessions = ConcurrentHashMap<String, String>()

    /**
     * Deletes the session with the specified id.
     * @param id The id of the session to invalidate.
     */
    override suspend fun invalidate(id: String) {
        sessions.remove(id)
    }

    /**
     * @return The token associated with the session id. If no session is found, throws a NoSuchElementException.
     */
    override suspend fun read(id: String): String {
        return sessions[id] ?: throw NoSuchElementException("No session found for $id")
    }

    /**
     * Writes the token to the session storage.
     * @param id The id of the session.
     * @param value The token to write.
     */
    override suspend fun write(id: String, value: String) {
        sessions[id] = value
    }


}
package com.klejdis.services.util

import io.github.cdimascio.dotenv.Dotenv
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.mindrot.jbcrypt.BCrypt

object PasswordUtil : KoinComponent {
    private val vault by inject<Dotenv>()
    private const val SALT_LENGTH = 16
    private const val HASH_LENGTH = 256
    private const val ITERATIONS = 65536

    fun generateSalt(): String {
        return BCrypt.gensalt()
    }

    fun hashString(input: String): HashedString {
        val hashedString = BCrypt.hashpw(input, BCrypt.gensalt())
        val lastSeparator = hashedString.lastIndexOf('$')
        val (version, hash) = (hashedString.substring(0, lastSeparator) to hashedString.substring(lastSeparator + 1))
        return HashedString(hash, version)
    }
    fun hashPassword(password: String, salt: String): String {
        return BCrypt.hashpw(password, salt)
    }


    fun isPasswordValid(password: String, storedHash: String, salt: String): Boolean {
        val hash = hashPassword(password, salt)
        return hash.contentEquals(storedHash)
    }
}
@Serializable
data class HashedString(val hash: String, val version: String)
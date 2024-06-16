package com.klejdis.services.util

import io.github.cdimascio.dotenv.Dotenv
import io.ktor.util.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.java.KoinJavaComponent.inject
import org.mindrot.jbcrypt.BCrypt
import java.security.MessageDigest
import java.security.SecureRandom
import java.security.spec.KeySpec
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import kotlin.text.toCharArray

object PasswordUtil : KoinComponent {
    private val vault by inject<Dotenv>()
    private const val SALT_LENGTH = 16
    private const val HASH_LENGTH = 256
    private const val ITERATIONS = 65536

    fun generateSalt(): String {
        return BCrypt.gensalt()
    }
    fun hashPassword(password: String, salt: String): String {
        return BCrypt.hashpw(password, salt)
    }


    fun isPasswordValid(password: String, storedHash: String, salt: String): Boolean {
        val hash = hashPassword(password, salt)
        return hash.contentEquals(storedHash)
    }
}
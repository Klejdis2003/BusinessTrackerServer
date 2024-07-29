package com.klejdis.services.util

import io.ktor.util.*
import java.nio.file.Files
import java.nio.file.Paths
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.KeySpec
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher

fun String.encryptRSA(): String = RSAEncryptionUtil.encrypt(this)
fun String.decryptRSA(): String = RSAEncryptionUtil.decrypt(this)

/**
 * Utility class for RSA encryption and decryption.
 */
private object RSAEncryptionUtil {
    private val KEY_FACTORY = KeyFactory.getInstance("RSA")
    private const val TRANSFORMATION = "RSA/ECB/PKCS1Padding"

    /**
     * Reads the key from the given file.
     * @param filename The name of the file containing the key.
     * @param keyType The type of the key to read. It is important to specify correctly,
     * as there are different methods for decoding public and private keys.
     */
    private fun readKey(filename: String, keyType: KeyType): KeySpec {
        val bytes = Files.readAllBytes(Paths.get(filename))
        var pemFile = String(bytes)
        pemFile = pemFile
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replace("\\s".toRegex(), "")

        return when(keyType){
            KeyType.PUBLIC -> X509EncodedKeySpec(pemFile.decodeBase64Bytes())
            KeyType.PRIVATE -> PKCS8EncodedKeySpec(pemFile.decodeBase64Bytes())
        }
    }

    /**
     * Reads the RSA public key from "public_key.pem" in root directory.
     * @return The public key object.
     */
    private fun readPublicKey(): PublicKey {
        val keySpec = readKey("public_key.pem", KeyType.PUBLIC)
        return KEY_FACTORY.generatePublic(keySpec)
    }

    /**
     * Reads the RSA private key from "private_key.pem" in root directory.
     * @return The private key object.
     */
    private fun readPrivateKey(): PrivateKey {
        val keySpec = readKey("private_key.pem", KeyType.PRIVATE)
        return KEY_FACTORY.generatePrivate(keySpec)
    }

    /**
     * Encrypts the given value using RSA encryption. The public key for encryption is extracted using
     * [readPublicKey].
     * @param value The value to encrypt.
     * @return The encrypted value, encoded in Base64.
     */
    fun encrypt(value: String): String =
        with(Cipher.getInstance(TRANSFORMATION)) {
            init(Cipher.ENCRYPT_MODE, readPublicKey())
            doFinal(value.toByteArray(Charsets.UTF_8)).encodeBase64()
        }

    /**
     * Decrypts the given value using RSA decryption. The private key for decryption is extracted using
     * [readPrivateKey].
     * @param value The encrypted value, encoded in Base64.
     * @return The decrypted value.
     */
    fun decrypt(value: String): String =
        with(Cipher.getInstance(TRANSFORMATION)) {
            init(Cipher.DECRYPT_MODE, readPrivateKey())
            String(doFinal(value.decodeBase64Bytes()), Charsets.UTF_8)
        }
}

/**
 * Types of RSA keys.
 */
private enum class KeyType {
    PUBLIC, PRIVATE
}


